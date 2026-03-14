package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.WarrantyStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWarrantyStatusRequest {

    @NotNull(message = "Warranty status cannot be null")
    private WarrantyStatus status;
}
