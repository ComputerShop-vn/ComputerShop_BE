package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    int productId;
    int categoriesId;
    int brandId;
    String name;
    String description;
    double price;
    Integer stockQuantity;
    List<String> serialNumbers;
    List<ProductAttributeResponse> attributes;
    List<String> images;

}
