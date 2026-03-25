package sp26.group3.computer.sba301_computershop.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByProductResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalRevenue;
    private List<TopProductDto> products;
}
