package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {
    @NotNull(message = "Category ID is required")
    Integer categoriesId;

    @NotNull(message = "Brand ID is required")
    Integer brandId;

    @NotNull(message = "Name is required")
    String name;

    String description;

    @NotNull(message = "Price is required")
    Double price;

    Integer stockQuantity;
    List<String> serialNumbers;

    List<ProductAttributeRequest> attributes;
    List<String> imageUrls;

}
