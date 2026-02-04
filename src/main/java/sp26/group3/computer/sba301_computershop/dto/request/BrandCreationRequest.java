package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandCreationRequest {
    private String brandName;
}
