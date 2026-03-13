package sp26.group3.computer.sba301_computershop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group3.computer.sba301_computershop.enums.ClaimStatus;
import sp26.group3.computer.sba301_computershop.enums.SolutionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClaimRequest {

    private ClaimStatus status;
    private String technicianNote;
    private SolutionType solutionType;
}
