package sp26.group3.computer.sba301_computershop.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentOrderDetailDto {
    private int orderId;
    private String customerUsername;
    private double orderTotal;
    private long totalInstallments;
    private long paidInstallments;
    private long remainingInstallments;
    private String nextDueDate;
}
