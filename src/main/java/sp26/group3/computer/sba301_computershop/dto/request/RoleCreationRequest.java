package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreationRequest {

    @NotBlank(message = "ROLE_NAME_REQUIRED")
    private String name;
}

