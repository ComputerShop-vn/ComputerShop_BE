package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateCartItemRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.CartResponse;
import sp26.group3.computer.sba301_computershop.service.CartService;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
public class CartController {

    CartService cartService;

    // ================= GET MY CART =================
    @GetMapping
    ApiResponse<CartResponse> getMyCart() {
        log.info("[GET] /cart - Get my cart");

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.getMyCart());

        log.info("[GET] /cart - SUCCESS | totalItems={}", apiResponse.getResult().getTotalItems());
        return apiResponse;
    }

    // ================= ADD ITEM TO CART =================
    @PostMapping("/items")
    ApiResponse<CartResponse> addToCart(@RequestBody @Valid AddToCartRequest request) {
        log.info("[POST] /cart/items - Add to cart | variantId={} qty={}",
                request.getVariantId(), request.getQuantity());

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.addToCart(request));

        log.info("[POST] /cart/items - SUCCESS | totalItems={}", apiResponse.getResult().getTotalItems());
        return apiResponse;
    }

    // ================= UPDATE CART ITEM QUANTITY =================
    @PutMapping("/items/{cartItemId}")
    ApiResponse<CartResponse> updateCartItem(
            @PathVariable int cartItemId,
            @RequestBody @Valid UpdateCartItemRequest request) {

        log.info("[PUT] /cart/items/{} - Update qty={}", cartItemId, request.getQuantity());

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.updateCartItem(cartItemId, request));

        log.info("[PUT] /cart/items/{} - SUCCESS", cartItemId);
        return apiResponse;
    }

    // ================= REMOVE CART ITEM =================
    @DeleteMapping("/items/{cartItemId}")
    ApiResponse<CartResponse> removeCartItem(@PathVariable int cartItemId) {
        log.info("[DELETE] /cart/items/{} - Remove cart item", cartItemId);

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.removeCartItem(cartItemId));

        log.info("[DELETE] /cart/items/{} - SUCCESS", cartItemId);
        return apiResponse;
    }

    // ================= CLEAR CART =================
    @DeleteMapping
    ApiResponse<CartResponse> clearCart() {
        log.info("[DELETE] /cart - Clear cart");

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.clearCart());

        log.info("[DELETE] /cart - SUCCESS");
        return apiResponse;
    }
}
