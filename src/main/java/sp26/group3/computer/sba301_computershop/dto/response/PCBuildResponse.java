package sp26.group3.computer.sba301_computershop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sp26.group3.computer.sba301_computershop.enums.BuildStatus;

import java.time.LocalDateTime;
import java.util.List;

// Dùng cho mutation endpoints (add/update/remove item, save build) — trả về trạng thái build sau khi thay đổi
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PCBuildResponse {

    int buildId;
    String buildName;
    BuildStatus status;
    double totalPrice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    List<PCBuildItemResponse> items;
}
