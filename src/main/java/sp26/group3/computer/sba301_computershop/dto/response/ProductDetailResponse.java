package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private int productId;
    private String name;
    private String description;
    
    private Double basePrice;
    private Double discountedPrice;
    
    private int categoryId;
    private String categoryName;
    
    private int brandId;
    private String brandName;
    private String brandLogoUrl;
    
    private List<String> imageUrls;
    
    private Double averageRating;
    private Integer totalReviews;
    
    private Boolean hasPromotion;
    private Double discountPercent;
    private String promoCode;
    
    private List<ProductVariantResponse> variants;
}
