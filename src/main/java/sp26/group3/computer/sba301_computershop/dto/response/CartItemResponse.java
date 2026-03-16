package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    int cartItemId;
    int variantId;
    String variantName;
    String sku;
    double price;              // Giá gốc
    Double discountedPrice;    // Giá sau giảm (null nếu không có khuyến mãi)
    double discountPercent;    // % giảm (0 nếu không có)
    int stockQuantity;
    int quantity;

    // Product info
    int productId;
    String productName;
    String thumbnailUrl;
}
