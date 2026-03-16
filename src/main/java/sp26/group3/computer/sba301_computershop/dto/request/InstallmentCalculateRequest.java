package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstallmentCalculateRequest {

    @NotNull(message = "Package ID is required")
    Integer packageId;

    @NotNull(message = "Order amount is required")
    Double orderAmount;
}
