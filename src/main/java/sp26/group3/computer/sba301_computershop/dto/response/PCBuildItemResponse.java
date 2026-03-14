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
public class PCBuildItemResponse {

    int buildItemId;
    String componentType;
    String componentTypeName; // Display name tiếng Việt

    // Variant info
    int variantId;
    String variantName;
    String sku;

    // Giá snapshot tại thời điểm chọn
    double price;
    int quantity;
    double subtotal; // price * quantity

    // Product info
    int productId;
    String productName;
    String thumbnailUrl;
}
