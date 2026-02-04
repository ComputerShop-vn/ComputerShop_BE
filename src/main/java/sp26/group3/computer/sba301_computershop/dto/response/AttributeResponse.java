package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributeResponse {
    private int attributeId;
    private String attributeName;
}