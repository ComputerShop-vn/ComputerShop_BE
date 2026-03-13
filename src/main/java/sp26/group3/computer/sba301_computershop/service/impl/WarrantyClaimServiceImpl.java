package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group3.computer.sba301_computershop.dto.request.ClaimCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateClaimRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ClaimResponse;
import sp26.group3.computer.sba301_computershop.entity.Warranty;
import sp26.group3.computer.sba301_computershop.entity.WarrantyClaim;
import sp26.group3.computer.sba301_computershop.enums.ClaimStatus;
import sp26.group3.computer.sba301_computershop.enums.WarrantyStatus;
import sp26.group3.computer.sba301_computershop.repository.WarrantyClaimRepository;
import sp26.group3.computer.sba301_computershop.repository.WarrantyRepository;
import sp26.group3.computer.sba301_computershop.service.WarrantyClaimService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WarrantyClaimServiceImpl implements WarrantyClaimService {

    WarrantyClaimRepository warrantyClaimRepository;
    WarrantyRepository warrantyRepository;

    @Override
    @Transactional
    public ClaimResponse createClaim(ClaimCreationRequest request) {
        log.info("Creating claim for warrantyId={}", request.getWarrantyId());

        Warranty warranty = warrantyRepository.findById(request.getWarrantyId())
                .orElseThrow(() -> new RuntimeException("Warranty not found with id: " + request.getWarrantyId()));

        if (warranty.getStatus() != WarrantyStatus.ACTIVE) {
            throw new RuntimeException(
                    "Cannot create claim. Warranty is not ACTIVE. Current status: " + warranty.getStatus());
        }

        WarrantyClaim claim = WarrantyClaim.builder()
                .warranty(warranty)
                .claimDate(LocalDate.now())
                .customerNote(request.getCustomerNote())
                .status(ClaimStatus.PENDING)
                .build();

        WarrantyClaim savedClaim = warrantyClaimRepository.save(claim);
        log.info("Created claim with id={}", savedClaim.getClaimId());

        return toClaimResponse(savedClaim);
    }

    @Override
    @Transactional
    public ClaimResponse updateClaim(int claimId, UpdateClaimRequest request) {
        log.info("Updating claim id={}", claimId);

        WarrantyClaim claim = warrantyClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));

        if (request.getStatus() != null) {
            claim.setStatus(request.getStatus());
        }
        if (request.getTechnicianNote() != null) {
            claim.setTechnicianNote(request.getTechnicianNote());
        }
        if (request.getSolutionType() != null) {
            claim.setSolutionType(request.getSolutionType());
        }

        if (request.getStatus() == ClaimStatus.COMPLETED || request.getStatus() == ClaimStatus.REJECTED) {
            if (claim.getReturnDate() == null) {
                claim.setReturnDate(LocalDate.now());
            }
        }

        WarrantyClaim updatedClaim = warrantyClaimRepository.save(claim);
        return toClaimResponse(updatedClaim);
    }

    @Override
    public List<ClaimResponse> getClaimsByWarrantyId(int warrantyId) {
        return warrantyClaimRepository.findByWarranty_Id(warrantyId)
                .stream()
                .map(this::toClaimResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClaimResponse getClaimById(int claimId) {
        WarrantyClaim claim = warrantyClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        return toClaimResponse(claim);
    }

    private ClaimResponse toClaimResponse(WarrantyClaim claim) {
        return ClaimResponse.builder()
                .claimId(claim.getClaimId())
                .warrantyId(claim.getWarranty().getId())
                .claimDate(claim.getClaimDate())
                .customerNote(claim.getCustomerNote())
                .technicianNote(claim.getTechnicianNote())
                .status(claim.getStatus())
                .solutionType(claim.getSolutionType())
                .returnDate(claim.getReturnDate())
                .build();
    }
}
