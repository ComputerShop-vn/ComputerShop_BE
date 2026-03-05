package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstallmentPackageRequest {

    @NotBlank(message = "Package name is required")
    String name;

    @NotNull(message = "Duration in months is required")
    Integer durationMonths;

    @NotNull(message = "Interest rate is required")
    Double interestRate;

    @NotNull(message = "Minimum order amount is required")
    Double minOrderAmount;

    @NotNull(message = "Active status is required")
    Boolean isActive;
}
