package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private int brandId;
    private String brandName;
    private String logoUrl;
}
