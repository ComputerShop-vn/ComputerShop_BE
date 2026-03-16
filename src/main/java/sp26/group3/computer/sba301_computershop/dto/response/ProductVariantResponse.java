package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductVariantResponse {

    private int variantId;
    private int productId;
    private String sku;
    private double price;           // Giá gốc
    private Double discountedPrice; // Giá sau giảm (null nếu không có KM)
    private double discountPercent; // % giảm (0 nếu không có)
    private int stockQuantity;
    private String variantName;
    private List<VariantAttributeResponse> attributes;
}
