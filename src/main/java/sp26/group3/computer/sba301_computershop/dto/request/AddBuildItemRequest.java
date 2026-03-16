package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;

// Dùng cho upsert: nếu componentType chưa có → add, đã có → overwrite
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddBuildItemRequest {

    @NotNull(message = "Component type is required")
    ComponentType componentType;

    @NotNull(message = "Variant ID is required")
    Integer variantId;

    @Builder.Default
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity = 1;
}
