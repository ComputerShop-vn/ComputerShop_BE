package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.ClaimCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateClaimRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ClaimResponse;

import java.util.List;

public interface WarrantyClaimService {
    ClaimResponse createClaim(ClaimCreationRequest request);

    ClaimResponse updateClaim(int claimId, UpdateClaimRequest request);

    List<ClaimResponse> getClaimsByWarrantyId(int warrantyId);

    ClaimResponse getClaimById(int claimId);
}
