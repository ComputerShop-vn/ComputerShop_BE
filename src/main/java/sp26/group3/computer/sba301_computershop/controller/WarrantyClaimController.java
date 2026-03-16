package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.ClaimCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UpdateClaimRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ClaimResponse;
import sp26.group3.computer.sba301_computershop.service.WarrantyClaimService;

import java.util.List;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarrantyClaimController {

    WarrantyClaimService warrantyClaimService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<ClaimResponse> createClaim(@RequestBody @Valid ClaimCreationRequest request) {
        ClaimResponse response = warrantyClaimService.createClaim(request);
        ApiResponse<ClaimResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<ClaimResponse> getClaimById(@PathVariable int id) {
        ClaimResponse response = warrantyClaimService.getClaimById(id);
        ApiResponse<ClaimResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/warranty/{warrantyId}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<List<ClaimResponse>> getClaimsByWarrantyId(@PathVariable int warrantyId) {
        List<ClaimResponse> response = warrantyClaimService.getClaimsByWarrantyId(warrantyId);
        ApiResponse<List<ClaimResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<ClaimResponse> updateClaim(
            @PathVariable int id,
            @RequestBody @Valid UpdateClaimRequest request) {
        ClaimResponse response = warrantyClaimService.updateClaim(id, request);
        ApiResponse<ClaimResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }
}
