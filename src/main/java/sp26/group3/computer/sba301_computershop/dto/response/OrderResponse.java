package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import sp26.group3.computer.sba301_computershop.enums.OrderStatus;
import sp26.group3.computer.sba301_computershop.enums.PaymentMethod;
import sp26.group3.computer.sba301_computershop.enums.PaymentMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    int orderId;
    int userId;
    String username;
    double totalAmount;
    OrderStatus status;
    PaymentMethod paymentMethod;
    PaymentMode paymentMode;
    LocalDateTime orderDate;
    List<OrderItemResponse> items;
    List<PaymentScheduleResponse> payments;
    String paymentUrl;
}
