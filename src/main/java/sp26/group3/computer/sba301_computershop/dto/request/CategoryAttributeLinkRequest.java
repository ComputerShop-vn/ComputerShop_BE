package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributeLinkRequest {
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Integer categoryId;

    @NotEmpty(message = "Attribute ID list must not be empty")
    private List<@Positive Integer> attributeIds;
}
