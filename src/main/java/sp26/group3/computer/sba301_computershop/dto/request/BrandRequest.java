package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.Data;

@Data
public class BrandRequest {
    private String brandName;
    private String logoUrl;
    // logoUrl sẽ là URL trả về từ Cloudinary
}
