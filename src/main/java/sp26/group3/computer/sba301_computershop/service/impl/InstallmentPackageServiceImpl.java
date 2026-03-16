package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.InstallmentPackageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.InstallmentPackageResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.entity.InstallmentPackage;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.InstallmentPackageRepository;
import sp26.group3.computer.sba301_computershop.service.InstallmentPackageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InstallmentPackageServiceImpl implements InstallmentPackageService {

    InstallmentPackageRepository installmentPackageRepository;

    @Override
    public List<InstallmentPackageResponse> getActivePackages() {
        return installmentPackageRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstallmentPackageResponse> getAllPackages() {
        return installmentPackageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<InstallmentPackageResponse> getAllPackagesPaged(Pageable pageable) {
        Page<InstallmentPackage> page = installmentPackageRepository.findAll(pageable);
        return PagedResponse.<InstallmentPackageResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public InstallmentPackageResponse createPackage(InstallmentPackageRequest request) {
        InstallmentPackage pkg = InstallmentPackage.builder()
                .name(request.getName())
                .durationMonths(request.getDurationMonths())
                .interestRate(request.getInterestRate())
                .minOrderAmount(request.getMinOrderAmount())
                .isActive(request.getIsActive())
                .build();
        return toResponse(installmentPackageRepository.save(pkg));
    }

    @Override
    public InstallmentPackageResponse updatePackage(int id, InstallmentPackageRequest request) {
        InstallmentPackage pkg = installmentPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment package not found"));

        if (request.getName() != null) {
            pkg.setName(request.getName());
        }
        if (request.getDurationMonths() != null) {
            pkg.setDurationMonths(request.getDurationMonths());
        }
        if (request.getInterestRate() != null) {
            pkg.setInterestRate(request.getInterestRate());
        }
        if (request.getMinOrderAmount() != null) {
            pkg.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getIsActive() != null) {
            pkg.setActive(request.getIsActive());
        }

        return toResponse(installmentPackageRepository.save(pkg));
    }

    @Override
    public void deletePackage(int id) {
        // Find if exists, then update isActive to false
        InstallmentPackage pkg = installmentPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment package not found"));
        pkg.setActive(false);
        installmentPackageRepository.save(pkg);
    }

    private InstallmentPackageResponse toResponse(InstallmentPackage pkg) {
        return InstallmentPackageResponse.builder()
                .packageId(pkg.getPackageId())
                .name(pkg.getName())
                .durationMonths(pkg.getDurationMonths())
                .interestRate(pkg.getInterestRate())
                .minOrderAmount(pkg.getMinOrderAmount())
                .isActive(pkg.isActive())
                .build();
    }
}
