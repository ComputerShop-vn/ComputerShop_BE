package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAttributeRequest {
    Integer attributeId;
    String attributeValue;
}
