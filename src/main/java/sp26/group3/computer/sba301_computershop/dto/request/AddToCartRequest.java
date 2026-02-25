package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartRequest {

    @NotNull(message = "Variant ID is required")
    Integer variantId;

    @Builder.Default
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity = 1;
}
