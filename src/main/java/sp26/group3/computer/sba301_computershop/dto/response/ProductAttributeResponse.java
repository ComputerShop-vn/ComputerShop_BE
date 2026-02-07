package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAttributeResponse {
    int attributeId;
    String attributeName;
    String attributeValue;
}
