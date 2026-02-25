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
    double price;
    int stockQuantity;
    int quantity;

    // Product info
    int productId;
    String productName;
    String productImageUrl;
}
