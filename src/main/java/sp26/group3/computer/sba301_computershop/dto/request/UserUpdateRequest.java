package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 15)
    private String phoneNumber;

    private String status;
}
