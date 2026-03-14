package sp26.group3.computer.sba301_computershop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.ComponentType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompatibleVariantsRequest {

    /** Các items đang có trong build (FE gửi lên, không cần buildId) */
    @NotNull
    @Valid
    List<ItemHint> currentItems;

    /** ComponentType muốn lấy filter hints */
    @NotNull
    ComponentType targetComponentType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemHint {
        @NotNull
        ComponentType componentType;

        @NotNull
        Integer variantId;
    }
}
