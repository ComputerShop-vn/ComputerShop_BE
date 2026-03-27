package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AddBuildItemRequest;
import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.CompatibleVariantsRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PlaceOrderRequest;
import sp26.group3.computer.sba301_computershop.dto.request.SaveBuildNameRequest;
import sp26.group3.computer.sba301_computershop.dto.response.*;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.enums.BuildStatus;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.CompatibilityService;
import sp26.group3.computer.sba301_computershop.service.OrderService;
import sp26.group3.computer.sba301_computershop.service.PCBuildService;
import sp26.group3.computer.sba301_computershop.repository.ProductVariantAttributeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PCBuildServiceImpl implements PCBuildService {

    PCBuildRepository pcBuildRepository;
    PCBuildItemRepository pcBuildItemRepository;
    ProductVariantRepository productVariantRepository;
    ProductVariantAttributeRepository productVariantAttributeRepository;
    ProductImageRepository productImageRepository;
    UserRepository userRepository;
    CompatibilityService compatibilityService;
    OrderService orderService;
    PromotionProductRepository promotionProductRepository;

    // ======================== UPSERT ITEM ========================

    @Override
    @Transactional
    public PCBuildResponse upsertItem(AddBuildItemRequest request) {
        User user = getCurrentUser();
        PCBuild build = pcBuildRepository
                .findByUserUserIdAndStatus(user.getUserId(), BuildStatus.DRAFT)
                .orElseGet(() -> createNewDraft(user));
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        // RAM slot check: kiểm tra tổng quantity sau khi upsert không vượt quá slot mainboard
        if (request.getComponentType() == ComponentType.RAM) {
            validateRamSlots(build, request.getVariantId(), request.getQuantity());
        }

        double price = effectivePrice(variant);
        boolean isMultiSlot = request.getComponentType().isMultiSlot();
        if (isMultiSlot) {
            pcBuildItemRepository
                    .findByBuildBuildIdAndComponentTypeAndVariantVariantId(
                            build.getBuildId(), request.getComponentType(), request.getVariantId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setQuantity(request.getQuantity());
                                existing.setPrice(price);
                                pcBuildItemRepository.save(existing);
                                log.info("[PCBuild] Updated multi-slot item | buildId={} componentType={} variantId={}",
                                        build.getBuildId(), request.getComponentType(), variant.getVariantId());
                            },
                            () -> {
                                pcBuildItemRepository.save(PCBuildItem.builder()
                                        .build(build)
                                        .componentType(request.getComponentType())
                                        .variant(variant)
                                        .quantity(request.getQuantity())
                                        .price(price)
                                        .build());
                                log.info("[PCBuild] Added multi-slot item | buildId={} componentType={} variantId={}",
                                        build.getBuildId(), request.getComponentType(), variant.getVariantId());
                            }
                    );
        } else {
            // Single-slot: overwrite nếu type đã có, insert nếu chưa
            pcBuildItemRepository
                    .findByBuildBuildIdAndComponentType(build.getBuildId(), request.getComponentType())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setVariant(variant);
                                existing.setPrice(price);
                                existing.setQuantity(request.getQuantity());
                                pcBuildItemRepository.save(existing);
                                log.info("[PCBuild] Overwrite item | buildId={} componentType={} variantId={}",
                                        build.getBuildId(), request.getComponentType(), variant.getVariantId());
                            },
                            () -> {
                                pcBuildItemRepository.save(PCBuildItem.builder()
                                        .build(build)
                                        .componentType(request.getComponentType())
                                        .variant(variant)
                                        .quantity(request.getQuantity())
                                        .price(price)
                                        .build());
                                log.info("[PCBuild] Added item | buildId={} componentType={} variantId={}",
                                        build.getBuildId(), request.getComponentType(), variant.getVariantId());
                            }
                    );
        }

        return reloadAndUpdate(build);
    }

    // ======================== SAVE DRAFT ========================

    @Override
    @Transactional
    public PCBuildResponse saveBuild(SaveBuildNameRequest request) {
        User user = getCurrentUser();
        PCBuild build = pcBuildRepository
                .findByUserUserIdAndStatus(user.getUserId(), BuildStatus.DRAFT)
                .orElseThrow(() -> new AppException(ErrorCode.PC_BUILD_NOT_FOUND));

        build.setBuildName(request.getBuildName());
        build.setStatus(BuildStatus.SAVED);
        build.setUpdatedAt(LocalDateTime.now());
        pcBuildRepository.save(build);

        log.info("[PCBuild] Saved build | buildId={} name='{}'", build.getBuildId(), request.getBuildName());
        return toBuildResponse(build);
    }

    // ======================== ORDER FROM BUILD ========================

    @Override
    @Transactional
    public OrderResponse orderFromBuild(PlaceOrderRequest request, HttpServletRequest httpRequest) {
        User user = getCurrentUser();
        // Hỗ trợ order từ cả DRAFT lẫn SAVED
        PCBuild build = pcBuildRepository
                .findByUserUserIdAndStatus(user.getUserId(), BuildStatus.DRAFT)
                .or(() -> pcBuildRepository
                        .findByUserUserIdAndStatusOrderByCreatedAtDesc(user.getUserId(), BuildStatus.SAVED)
                        .stream().findFirst())
                .orElseThrow(() -> new AppException(ErrorCode.PC_BUILD_NOT_FOUND));

        List<PCBuildItem> items = build.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.PC_BUILD_INCOMPLETE);
        }

        List<AddToCartRequest> orderItems = items.stream()
                .map(i -> AddToCartRequest.builder()
                        .variantId(i.getVariant().getVariantId())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        OrderResponse orderResponse = orderService.placeOrderFromItems(orderItems, request, httpRequest);

        build.setStatus(BuildStatus.ORDERED);
        build.setUpdatedAt(LocalDateTime.now());
        pcBuildRepository.save(build);

        log.info("[PCBuild] Ordered from build | buildId={} orderId={}", build.getBuildId(), orderResponse.getOrderId());
        return orderResponse;
    }

    // ======================== COMPATIBLE VARIANTS ========================

    @Override
    public CompatibleVariantsResponse getCompatibleVariants(
            List<CompatibleVariantsRequest.ItemHint> items, ComponentType targetType) {
        return compatibilityService.getFilterHints(items, targetType);
    }

    // ======================== REMOVE ITEM ========================

    @Override
    @Transactional
    public PCBuildResponse removeItem(int buildItemId) {
        User user = getCurrentUser();
        PCBuildItem item = pcBuildItemRepository.findById(buildItemId)
                .orElseThrow(() -> new AppException(ErrorCode.PC_BUILD_NOT_FOUND));

        PCBuild build = item.getBuild();
        if (build.getStatus() != BuildStatus.DRAFT
                || build.getUser().getUserId() != user.getUserId()) {
            throw new AppException(ErrorCode.PC_BUILD_NOT_FOUND);
        }

        pcBuildItemRepository.delete(item);
        log.info("[PCBuild] Removed item | buildItemId={} buildId={}", buildItemId, build.getBuildId());
        return reloadAndUpdate(build);
    }

    // ======================== PRIVATE HELPERS ========================

    /** Trả về giá sau khuyến mãi nếu có, ngược lại trả về giá gốc */
    private double effectivePrice(ProductVariant variant) {
        return promotionProductRepository
                .findActivePromotionByProductId(variant.getProduct().getProductId())
                .stream().findFirst()
                .map(pp -> variant.getPrice() * (1 - pp.getPromotion().getDiscountPercent() / 100.0))
                .orElse(variant.getPrice());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private void validateRamSlots(PCBuild build, int upsertVariantId, int newQuantity) {
        Optional<PCBuildItem> mainboardOpt = pcBuildItemRepository
                .findByBuildBuildIdAndComponentType(build.getBuildId(), ComponentType.MAINBOARD);
        if (mainboardOpt.isEmpty()) return; // chưa có mainboard → chưa biết giới hạn

        String ramSlotsStr = productVariantAttributeRepository
                .findByVariantVariantId(mainboardOpt.get().getVariant().getVariantId())
                .stream()
                .filter(a -> a.getAttribute().getAttributeName().equalsIgnoreCase("RAM Slots"))
                .map(ProductVariantAttribute::getValue)
                .findFirst()
                .orElse(null);
        if (ramSlotsStr == null) return;

        int maxSlots;
        try {
            maxSlots = Integer.parseInt(ramSlotsStr.trim());
        } catch (NumberFormatException e) {
            return;
        }

        // Tính tổng quantity của tất cả RAM hiện tại, trừ variant đang upsert (sẽ bị overwrite)
        int usedSlots = pcBuildItemRepository
                .findAllByBuildBuildIdAndComponentType(build.getBuildId(), ComponentType.RAM)
                .stream()
                .filter(i -> i.getVariant().getVariantId() != upsertVariantId)
                .mapToInt(PCBuildItem::getQuantity)
                .sum();

        if (usedSlots + newQuantity > maxSlots) {
            log.warn("[PCBuild] RAM slots exceeded: buildId={} maxSlots={} usedSlots={} newQty={}",
                    build.getBuildId(), maxSlots, usedSlots, newQuantity);
            throw new AppException(ErrorCode.RAM_SLOTS_EXCEEDED);
        }
    }

    private PCBuild createNewDraft(User user) {
        LocalDateTime now = LocalDateTime.now();
        PCBuild draft = PCBuild.builder()
                .user(user)
                .status(BuildStatus.DRAFT)
                .totalPrice(0.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        log.info("[PCBuild] Created new DRAFT build for userId={}", user.getUserId());
        return pcBuildRepository.save(draft);
    }

    // ======================== GET DRAFT ========================

    @Override
    @Transactional(readOnly = true)
    public PCBuildResponse getDraft() {
        User user = getCurrentUser();
        PCBuild build = pcBuildRepository
                .findByUserUserIdAndStatus(user.getUserId(), BuildStatus.DRAFT)
                .orElseGet(() -> createNewDraft(user));
        return toBuildResponse(build);
    }

    // ======================== GET MY BUILDS ========================

    @Override
    @Transactional(readOnly = true)
    public List<PCBuildResponse> getMyBuilds() {
        User user = getCurrentUser();
        return pcBuildRepository
                .findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(this::toBuildResponse)
                .toList();
    }

    private PCBuildResponse reloadAndUpdate(PCBuild build) {
        // Use JOIN FETCH query to bypass Hibernate L1 cache and get fresh items from DB
        PCBuild fresh = pcBuildRepository.findWithItemsById(build.getBuildId()).orElseThrow();
        double total = fresh.getItems().stream()
                .mapToDouble(i -> effectivePrice(i.getVariant()) * i.getQuantity())
                .sum();
        fresh.setTotalPrice(total);
        fresh.setUpdatedAt(LocalDateTime.now());
        pcBuildRepository.save(fresh);
        return toBuildResponse(fresh);
    }

    // ======================== MAPPERS ========================

    private PCBuildResponse toBuildResponse(PCBuild build) {
        List<PCBuildItemResponse> itemResponses = build.getItems() == null ? List.of()
                : build.getItems().stream().map(this::toItemResponse).toList();

        return PCBuildResponse.builder()
                .buildId(build.getBuildId())
                .buildName(build.getBuildName())
                .status(build.getStatus())
                .totalPrice(build.getTotalPrice())
                .createdAt(build.getCreatedAt())
                .updatedAt(build.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    private PCBuildItemResponse toItemResponse(PCBuildItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        String thumbnailUrl = productImageRepository
                .findFirstByProductProductIdAndIsThumbnailTrue(product.getProductId())
                .or(() -> productImageRepository.findByProductProductId(product.getProductId()).stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        // Tính discount giống CartService
        Double discountedPrice = null;
        double discountPercent = 0.0;
        var promos = promotionProductRepository.findActivePromotionByProductId(product.getProductId());
        if (!promos.isEmpty()) {
            discountPercent = promos.get(0).getPromotion().getDiscountPercent();
            discountedPrice = variant.getPrice() * (1 - discountPercent / 100.0);
        }

        double effectivePrice = discountedPrice != null ? discountedPrice : variant.getPrice();

        return PCBuildItemResponse.builder()
                .buildItemId(item.getBuildItemId())
                .componentType(item.getComponentType().name())
                .componentTypeName(item.getComponentType().getDisplayName())
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .discountedPrice(discountedPrice)
                .discountPercent(discountPercent)
                .quantity(item.getQuantity())
                .subtotal(effectivePrice * item.getQuantity())
                .productId(product.getProductId())
                .productName(product.getName())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
