package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantUpdateDTO {

    // Null = tạo mới, có value = update existing
    private Integer variantId;

    private String sku;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;

    private String variantName;

    private Map<Integer, String> attributes;

    // Nếu true, variant sẽ bị xóa (soft delete)
    private Boolean deleted;
}
