package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.AddToCartRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateCartItemRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CartResponse;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addToCart(AddToCartRequest request);
    CartResponse updateCartItem(int cartItemId, UpdateCartItemRequest request);
    CartResponse removeCartItem(int cartItemId);
    CartResponse clearCart();
}
