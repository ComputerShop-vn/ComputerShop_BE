package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100)
    private String categoryName;

    // Optional parent category id for hierarchical categories
    private Integer parentCategoryId;
}
