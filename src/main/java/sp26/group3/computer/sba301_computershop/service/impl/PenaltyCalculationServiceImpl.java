package sp26.group3.computer.sba301_computershop.service.impl;

import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.service.PenaltyCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PenaltyCalculationServiceImpl implements PenaltyCalculationService {

    @Override
    public double calculateDailyPenalty(double amount, double annualPenaltyRate) {
        BigDecimal installmentAmount = BigDecimal.valueOf(amount);
        BigDecimal rate = BigDecimal.valueOf(annualPenaltyRate);
        BigDecimal divisor = BigDecimal.valueOf(100);
        BigDecimal daysInYear = BigDecimal.valueOf(365);
        BigDecimal divisorTotal = divisor.multiply(daysInYear); // 36500

        // Calculate daily penalty for 1 day
        // Formula: Amount * (AnnualRate / 100 / 365) * 1
        BigDecimal dailyPenalty = installmentAmount
                .multiply(rate)
                .divide(divisorTotal, 10, RoundingMode.HALF_UP);

        // Round to 2 decimal places (common for currency)
        return dailyPenalty.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
