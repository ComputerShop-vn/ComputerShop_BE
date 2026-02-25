package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

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
    String status;
    LocalDateTime orderDate;
    List<OrderItemResponse> items;
    List<PaymentScheduleResponse> payments;
}
