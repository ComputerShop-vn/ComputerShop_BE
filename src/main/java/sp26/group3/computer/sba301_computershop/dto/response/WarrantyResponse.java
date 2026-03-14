package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.WarrantyStatus;
import sp26.group3.computer.sba301_computershop.enums.WarrantyType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyResponse {
    private int id;
    private int orderItemId;
    private int productId;
    private String productName;
    private String serialNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private WarrantyStatus status;
    private WarrantyType type;
}
