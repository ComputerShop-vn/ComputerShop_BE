package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.PromotionUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PromotionResponse;
import sp26.group3.computer.sba301_computershop.entity.Product;
import sp26.group3.computer.sba301_computershop.entity.Promotion;
import sp26.group3.computer.sba301_computershop.entity.PromotionProduct;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.PromotionMapper;
import sp26.group3.computer.sba301_computershop.repository.BrandRepository;
import sp26.group3.computer.sba301_computershop.repository.CategoryRepository;
import sp26.group3.computer.sba301_computershop.repository.ProductRepository;
import sp26.group3.computer.sba301_computershop.repository.PromotionProductRepository;
import sp26.group3.computer.sba301_computershop.repository.PromotionRepository;
import sp26.group3.computer.sba301_computershop.service.PromotionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;
    ProductRepository productRepository;
    BrandRepository brandRepository;
    CategoryRepository categoryRepository;
    PromotionProductRepository promotionProductRepository;

    @Override
    public PromotionResponse createPromotion(PromotionCreationRequest request) {
        log.info("Creating promotion with code: {}", request.getPromoCode());

        // Check if promo code already exists
        if (promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new AppException(ErrorCode.PROMO_CODE_EXISTED);
        }

        Promotion promotion = promotionMapper.toPromotion(request);
        Promotion savedPromotion = promotionRepository.save(promotion);

        log.info("Promotion created successfully with id: {}", savedPromotion.getPromotionId());

        return promotionMapper.toPromotionResponse(savedPromotion);
    }

    @Override
    public PromotionResponse updatePromotion(int promotionId, PromotionUpdateRequest request) {
        log.info("Updating promotion with id: {}", promotionId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        // Check if promo code is being changed and if it already exists
        if (request.getPromoCode() != null &&
                !request.getPromoCode().equals(promotion.getPromoCode()) &&
                promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new AppException(ErrorCode.PROMO_CODE_EXISTED);
        }

        promotionMapper.updatePromotion(promotion, request);
        Promotion updatedPromotion = promotionRepository.save(promotion);

        log.info("Promotion updated successfully with id: {}", promotionId);

        return promotionMapper.toPromotionResponse(updatedPromotion);
    }

    @Override
    public PromotionResponse getPromotionById(int promotionId) {
        log.info("Getting promotion with id: {}", promotionId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PromotionResponse getPromotionByCode(String promoCode) {
        log.info("Getting promotion with code: {}", promoCode);

        Promotion promotion = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        log.info("Getting all promotions");

        return promotionRepository.findAll()
                .stream()
                .map(promotionMapper::toPromotionResponse)
                .toList();
    }

    @Override
    public void deletePromotion(int promotionId) {
        log.warn("Deleting promotion with id: {}", promotionId);

        if (!promotionRepository.existsById(promotionId)) {
            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        // Xóa các bản ghi liên kết trước để tránh FK constraint violation
        promotionProductRepository.deleteByPromotionPromotionId(promotionId);

        promotionRepository.deleteById(promotionId);
        log.info("Promotion deleted successfully with id: {}", promotionId);
    }

    @Override
    @Transactional
    public void addPromotionToProducts(int promotionId, List<Integer> productIds) {
        log.info("Adding promotion {} to {} products", promotionId, productIds.size());

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        int addedCount = 0;
        for (Integer productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (promotionProductRepository.existsByProductIdAndPromotionId(productId, promotionId)) {
                log.warn("Product {} already has promotion {}, skipping", productId, promotionId);
                continue;
            }

            PromotionProduct promotionProduct = PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .build();

            promotionProductRepository.save(promotionProduct);
            addedCount++;
        }

        log.info("Successfully added promotion {} to {}/{} products", promotionId, addedCount, productIds.size());
    }

    @Override
    @Transactional
    public void addPromotionToCategory(int promotionId, int categoryId) {
        log.info("Adding promotion {} to all products in category {}", promotionId, categoryId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Product> products = productRepository.findByCategoryCategoryId(categoryId);

        int addedCount = 0;
        for (Product product : products) {
            if (promotionProductRepository.existsByProductIdAndPromotionId(product.getProductId(), promotionId)) {
                log.warn("Product {} already has promotion {}, skipping", product.getProductId(), promotionId);
                continue;
            }

            PromotionProduct promotionProduct = PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .build();

            promotionProductRepository.save(promotionProduct);
            addedCount++;
        }

        log.info("Successfully added promotion {} to {}/{} products in category {}", promotionId, addedCount, products.size(), categoryId);
    }

    @Override
    @Transactional
    public void addPromotionToBrand(int promotionId, int brandId) {
        log.info("Adding promotion {} to all products of brand {}", promotionId, brandId);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        List<Product> products = productRepository.findByBrandBrandId(brandId);

        int addedCount = 0;
        for (Product product : products) {
            if (promotionProductRepository.existsByProductIdAndPromotionId(product.getProductId(), promotionId)) {
                log.warn("Product {} already has promotion {}, skipping", product.getProductId(), promotionId);
                continue;
            }

            PromotionProduct promotionProduct = PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .build();

            promotionProductRepository.save(promotionProduct);
            addedCount++;
        }

        log.info("Successfully added promotion {} to {}/{} products of brand {}", promotionId, addedCount, products.size(), brandId);
    }
}