package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PromotionResponse {
    private int promotionId;
    private String promoCode;
    private int discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}