package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstallmentPreviewResponse {
    double orderAmount;
    double downPaymentPercentage;
    double downPaymentAmount;
    double remainingBalance;
    double monthlyInstallmentAmount;
    double interestRate;
    int durationMonths;
    double totalPayableAmount;
    List<PaymentScheduleResponse> schedule;
}
