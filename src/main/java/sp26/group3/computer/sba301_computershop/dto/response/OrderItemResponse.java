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
public class OrderItemResponse {

    int orderItemId;
    int quantity;
    double unitPrice;
    double subtotal;

    // Variant info
    int variantId;
    String variantName;
    String sku;

    // Product info
    int productId;
    String productName;
    String thumbnailUrl;

    // Shipping info
    String recipientName;
    String recipientPhone;
    String shippingAddress;

    // ProductItem info
    String serialNumber;
}
