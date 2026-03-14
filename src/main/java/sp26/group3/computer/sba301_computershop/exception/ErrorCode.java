package sp26.group3.computer.sba301_computershop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "User already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1002, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1012, "Role already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1011, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_SELF_PARENT(1013, "Category cannot be parent of itself", HttpStatus.BAD_REQUEST),
    CATEGORY_HAS_CHILDREN(1014, "Cannot delete category with child categories", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_KEY(1005, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_BODY(1008, "Invalid JSON request body", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1010, "User not existed", HttpStatus.NOT_FOUND),
    // Create user errors
    EMAIL_INVALID(1006, "EMAIL_INVALID", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1009, "EMAIL_REQUIRED", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1007, "PASSWORD_INVALID", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1008, "PASSWORD_REQUIRED", HttpStatus.BAD_REQUEST),
    // Blog errors
    BLOG_NOT_FOUND(2001, "Blog not found", HttpStatus.NOT_FOUND),
    BLOG_TITLE_EXISTED(2002, "Blog title already exists", HttpStatus.BAD_REQUEST),

    // Promotion errors
    PROMOTION_NOT_FOUND(3001, "Promotion not found", HttpStatus.NOT_FOUND),
    PROMO_CODE_EXISTED(3002, "Promo code already exists", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(3003, "Promotion has expired", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_PERCENT(3004, "Discount percent must be between 1 and 100", HttpStatus.BAD_REQUEST),

    // Attribute errors
    ATTRIBUTE_NOT_FOUND(4001, "Attribute not found", HttpStatus.NOT_FOUND),
    ATTRIBUTE_NAME_EXISTED(4002, "Attribute name already exists", HttpStatus.BAD_REQUEST),

    // Brand errors
    BRAND_NOT_FOUND(5001, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_NAME_EXISTED(5002, "Brand name already exists", HttpStatus.BAD_REQUEST),

    // Product errors
    PRODUCT_NOT_FOUND(6001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_NAME_EXISTED(6002, "Product name already exists", HttpStatus.BAD_REQUEST),

    // Product Variant errors
    VARIANT_NOT_FOUND(6101, "Product variant not found", HttpStatus.NOT_FOUND),
    SKU_EXISTED(6102, "SKU already exists", HttpStatus.BAD_REQUEST),

    // File errors
    FILE_UPLOAD_FAILED(7001, "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(7002, "Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR),
    MAX_UPLOAD_SIZE_EXCEEDED(7003, "File size exceeds the maximum allowed limit (10MB per file)",
            HttpStatus.BAD_REQUEST),

    // Cart errors
    CART_NOT_FOUND(8001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(8002, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_ITEM_ALREADY_EXISTS(8003, "Variant already exists in cart", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(8004, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),

    // Order errors
    ORDER_NOT_FOUND(9001, "Order not found", HttpStatus.NOT_FOUND),
    EMPTY_CART(9002, "Cart is empty, cannot place order", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(9003, "Insufficient stock for variant", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PAID(9004, "Order is already fully paid", HttpStatus.BAD_REQUEST),

    // PC Build errors
    PC_BUILD_NOT_FOUND(10001, "PC build not found", HttpStatus.NOT_FOUND),
    PC_BUILD_ACCESS_DENIED(10002, "You do not have access to this PC build", HttpStatus.FORBIDDEN),
    PC_BUILD_INCOMPLETE(10003, "PC build has no items", HttpStatus.BAD_REQUEST),
    PC_BUILD_INVALID(10004, "PC build has compatibility issues", HttpStatus.BAD_REQUEST),
    RAM_SLOTS_EXCEEDED(10005, "Mainboard RAM slots are full", HttpStatus.BAD_REQUEST),
    PC_BUILD_ITEM_NOT_FOUND(10006, "PC build item not found", HttpStatus.NOT_FOUND),

    ;

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
