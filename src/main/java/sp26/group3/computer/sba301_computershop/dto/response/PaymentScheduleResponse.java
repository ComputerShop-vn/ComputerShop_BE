package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentScheduleResponse {

    int paymentScheduleId;
    PaymentType paymentType;
    String providerName;
    int durationMonths;
    double interestRate;
    double totalAmount;
    int installmentNo;
    double amount;
    LocalDate dueDate;
    LocalDate paidDate;
    PaymentStatus status;
}
