package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private int userId;

    private String username;

    private String email;

    private String phoneNumber;

    private String status;

    private String roleName;

    private LocalDateTime createdAt;
}
