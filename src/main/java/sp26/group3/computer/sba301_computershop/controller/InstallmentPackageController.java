package sp26.group3.computer.sba301_computershop.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import sp26.group3.computer.sba301_computershop.dto.request.InstallmentPackageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.InstallmentPackageResponse;
import sp26.group3.computer.sba301_computershop.service.InstallmentPackageService;

import java.util.List;

@RestController
@RequestMapping("/installment-packages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InstallmentPackageController {

    InstallmentPackageService installmentPackageService;

    @GetMapping("/active")
    public ApiResponse<List<InstallmentPackageResponse>> getActivePackages() {
        log.info("[GET] /installment-packages/active - Get all active installment packages");
        ApiResponse<List<InstallmentPackageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(installmentPackageService.getActivePackages());
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<InstallmentPackageResponse>> getAllPackages() {
        log.info("[GET] /installment-packages - Get all installment packages");
        ApiResponse<List<InstallmentPackageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(installmentPackageService.getAllPackages());
        return apiResponse;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<InstallmentPackageResponse> createPackage(
            @RequestBody @Valid InstallmentPackageRequest request) {
        log.info("[POST] /installment-packages - Create installment package");
        ApiResponse<InstallmentPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(installmentPackageService.createPackage(request));
        return apiResponse;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<InstallmentPackageResponse> updatePackage(
            @PathVariable int id,
            @RequestBody InstallmentPackageRequest request) {
        log.info("[PUT] /installment-packages/{} - Update installment package", id);
        ApiResponse<InstallmentPackageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(installmentPackageService.updatePackage(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deletePackage(@PathVariable int id) {
        log.info("[DELETE] /installment-packages/{} - Delete installment package", id);
        installmentPackageService.deletePackage(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Installment package deleted successfully");
        return apiResponse;
    }
}
