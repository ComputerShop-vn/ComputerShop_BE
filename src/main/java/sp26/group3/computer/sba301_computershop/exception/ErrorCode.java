package sp26.group3.computer.sba301_computershop.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "User already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1002, "Role not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1011, "Category not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_KEY(1005, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(10010, "User not existed",HttpStatus.NOT_FOUND),
//   Create user errors
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
