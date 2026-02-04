package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PromotionUpdateRequest {
    private String promoCode;

    @Min(value = 1, message = "Discount percent must be at least 1")
    @Max(value = 100, message = "Discount percent must not exceed 100")
    private Integer discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;
}