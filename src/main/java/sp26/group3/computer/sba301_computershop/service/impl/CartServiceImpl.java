package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateCartItemRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CartItemResponse;
import sp26.group3.computer.sba301_computershop.dto.response.CartResponse;
import sp26.group3.computer.sba301_computershop.entity.*;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.*;
import sp26.group3.computer.sba301_computershop.service.CartService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productVariantRepository;
    ProductImageRepository productImageRepository;
    UserRepository userRepository;

    // ======================== GET MY CART ========================

    @Override
    public CartResponse getMyCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return toCartResponse(cart);
    }

    // ======================== ADD TO CART ========================

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        int stock = variant.getStockQuantity();

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartCartIdAndVariantVariantId(cart.getCartId(), variant.getVariantId());

        if (existingItem.isPresent()) {

            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (newQuantity > stock) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);

            log.info("Merged cart item | cartItemId={} newQty={}", item.getCartItemId(), item.getQuantity());

        } else {

            if (request.getQuantity() > stock) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();

            cartItemRepository.save(newItem);

            log.info("Added new cart item | variantId={} qty={}", variant.getVariantId(), request.getQuantity());
        }

        cart = cartRepository.findById(cart.getCartId()).orElseThrow();
        return toCartResponse(cart);
    }
    // ======================== UPDATE CART ITEM ========================

    @Override
    @Transactional
    public CartResponse updateCartItem(int cartItemId, UpdateCartItemRequest request) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Ensure item belongs to current user's cart
        if (item.getCart().getCartId() != cart.getCartId()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.info("Updated cart item | cartItemId={} newQty={}", cartItemId, request.getQuantity());

        cart = cartRepository.findById(cart.getCartId()).orElseThrow();
        return toCartResponse(cart);
    }

    // ======================== REMOVE CART ITEM ========================

    @Override
    @Transactional
    public CartResponse removeCartItem(int cartItemId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (item.getCart().getCartId() != cart.getCartId()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cart.getCartItems().remove(item);
        cartRepository.save(cart);
        log.info("Removed cart item | cartItemId={}", cartItemId);

        cart = cartRepository.findById(cart.getCartId()).orElseThrow();
        return toCartResponse(cart);
    }

    // ======================== CLEAR CART ========================

    @Override
    @Transactional
    public CartResponse clearCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cartItemRepository.deleteAllByCartCartId(cart.getCartId());
        log.info("Cleared cart | cartId={}", cart.getCartId());

        cart = cartRepository.findById(cart.getCartId()).orElseThrow();
        cart.getCartItems().clear();
        return toCartResponse(cart);
    }

    // ======================== HELPER METHODS ========================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .cartItems(new ArrayList<>())
                            .build();
                    log.info("Created new cart for userId={}", user.getUserId());
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cart.getCartItems() != null ? cart.getCartItems() : List.of();

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
                .toList();

        double totalPrice = itemResponses.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .totalItems(totalItems)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        // Get thumbnail image, or fallback to first image
        String imageUrl = productImageRepository
                .findFirstByProductProductIdAndIsThumbnailTrue(product.getProductId())
                .or(() -> productImageRepository.findByProductProductId(product.getProductId()).stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .quantity(item.getQuantity())
                .productId(product.getProductId())
                .productName(product.getName())
                .thumbnailUrl(imageUrl)
                .build();
    }
}
