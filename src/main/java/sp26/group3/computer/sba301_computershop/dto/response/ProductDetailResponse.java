package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for product detail page
 * Contains full information including description, images, reviews, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

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
    
    private List<String> imageUrls;
    
    private Double averageRating;
    private Integer totalReviews;
    
    private Boolean hasPromotion;
    private Double discountPercent;
    private String promoCode;
}
