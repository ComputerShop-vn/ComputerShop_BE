package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddPromotionToBrandRequest {

    @NotNull(message = "Promotion ID is required")
    private Integer promotionId;

    @NotNull(message = "Brand ID is required")
    private Integer brandId;
}
