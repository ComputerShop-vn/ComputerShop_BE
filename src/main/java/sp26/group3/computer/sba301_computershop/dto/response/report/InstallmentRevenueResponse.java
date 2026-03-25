package sp26.group3.computer.sba301_computershop.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentRevenueResponse {
    private InstallmentSummaryDto summary;
    private List<InstallmentOrderDetailDto> orders;
}
