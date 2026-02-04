package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributeCreationRequest {

    @NotBlank(message = "Attribute name is required")
    private String attributeName;
}