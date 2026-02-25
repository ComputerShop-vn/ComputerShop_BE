package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProductVariantResponse {

    private int variantId;
    private int productId;
    private String sku;
    private double price;
    private int stockQuantity;
    private String variantName;
    private Map<String, String> attributes;
}
