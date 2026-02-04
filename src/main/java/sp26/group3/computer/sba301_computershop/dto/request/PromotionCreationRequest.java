package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PromotionCreationRequest {

    @NotBlank(message = "Promo code is required")
    private String promoCode;

    @NotNull(message = "Discount percent is required")
    @Min(value = 1, message = "Discount percent must be at least 1")
    @Max(value = 100, message = "Discount percent must not exceed 100")
    private Integer discountPercent;

    private LocalDate startDate;

    private LocalDate endDate;
}