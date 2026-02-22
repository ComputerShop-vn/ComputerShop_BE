package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private int productId;
    private String name;
    private String description;
    private double price;
    private Double discountedPrice;
    private int stockQuantity;
    
    private int categoryId;
    private String categoryName;
    
    private int brandId;
    private String brandName;
    private String brandLogoUrl;
}
