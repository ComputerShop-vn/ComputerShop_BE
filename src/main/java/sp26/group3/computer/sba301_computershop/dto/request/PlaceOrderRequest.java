package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.PaymentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceOrderRequest {

    @NotBlank(message = "Recipient name is required")
    String recipientName;

    @NotBlank(message = "Recipient phone is required")
    String recipientPhone;

    @NotBlank(message = "Shipping address is required")
    String shippingAddress;

    @NotNull(message = "Payment type is required (FULL or INSTALLMENT)")
    PaymentType paymentType;

    // Only required for INSTALLMENT
    Integer packageId;
}
