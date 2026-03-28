package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.PaymentStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentScheduleResponse {

    int paymentScheduleId;
    int installmentNo;
    double amount;
    double penaltyAmount;
    LocalDate dueDate;
    LocalDate paidDate;
    String vnpTransactionNo;
    PaymentStatus status;
}
