package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.Data;

@Data
public class OtpRequest {
    private String email;
    private Integer otp;
}