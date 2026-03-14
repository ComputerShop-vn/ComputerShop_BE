package sp26.group3.computer.sba301_computershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.ClaimStatus;
import sp26.group3.computer.sba301_computershop.enums.SolutionType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private int claimId;
    private int warrantyId;
    private LocalDate claimDate;
    private String customerNote;
    private String technicianNote;
    private ClaimStatus status;
    private SolutionType solutionType;
    private LocalDate returnDate;
}
