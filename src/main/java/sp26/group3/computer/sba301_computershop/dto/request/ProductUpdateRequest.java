package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    private String name;

    private String description;

    private Integer categoryId;

    private Integer brandId;

    private Integer warrantyMonths;

    @Valid
    private List<VariantUpdateDTO> variants;

}
