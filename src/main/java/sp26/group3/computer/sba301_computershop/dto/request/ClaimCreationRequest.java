package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCreationRequest {

    private int warrantyId;

    @NotBlank(message = "Customer note cannot be empty")
    private String customerNote;
}
