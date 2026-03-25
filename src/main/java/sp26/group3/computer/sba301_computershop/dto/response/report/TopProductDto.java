package sp26.group3.computer.sba301_computershop.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private String productName;
    private String variantName;
    private long totalSold;
    private double revenue;
}
