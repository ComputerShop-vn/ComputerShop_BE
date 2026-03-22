package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstallmentPackageResponse {
    int packageId;
    String name;
    int durationMonths;
    double interestRate;
    double minOrderAmount;
    double downPaymentPercentage;
    boolean isActive;
    double annualPenaltyRate;
}
