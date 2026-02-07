package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeNameRequest {
    @NotBlank(message = "Attribute name is required")
    private String attributeName;
}
