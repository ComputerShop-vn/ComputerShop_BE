package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddPromotionToProductsRequest {

    @NotNull(message = "Promotion ID is required")
    private Integer promotionId;

    @NotEmpty(message = "Product IDs list cannot be empty")
    private List<Integer> productIds;
}
