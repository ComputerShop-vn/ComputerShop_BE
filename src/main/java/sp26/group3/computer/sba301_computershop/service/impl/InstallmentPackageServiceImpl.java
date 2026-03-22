package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.InstallmentCalculateRequest;
import sp26.group3.computer.sba301_computershop.dto.request.InstallmentPackageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.InstallmentPackageResponse;
import sp26.group3.computer.sba301_computershop.dto.response.InstallmentPreviewResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PaymentScheduleResponse;
import sp26.group3.computer.sba301_computershop.entity.InstallmentPackage;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.repository.InstallmentPackageRepository;
import sp26.group3.computer.sba301_computershop.service.InstallmentPackageService;

import java.time.LocalDate;
import java.util.ArrayList;
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
                .downPaymentPercentage(request.getDownPaymentPercentage())
                .isActive(request.getIsActive())
                .annualPenaltyRate(request.getAnnualPenaltyRate())
                .build();
        return toResponse(installmentPackageRepository.save(pkg));
    }

    @Override
    public InstallmentPackageResponse updatePackage(int id, InstallmentPackageRequest request) {
        InstallmentPackage pkg = installmentPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

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
        if (request.getDownPaymentPercentage() != null) {
            pkg.setDownPaymentPercentage(request.getDownPaymentPercentage());
        }
        if (request.getIsActive() != null) {
            pkg.setActive(request.getIsActive());
        }
        if (request.getAnnualPenaltyRate() != null) {
            pkg.setAnnualPenaltyRate(request.getAnnualPenaltyRate());
        }

        return toResponse(installmentPackageRepository.save(pkg));
    }

    @Override
    public void deletePackage(int id) {
        // Find if exists, then update isActive to false
        InstallmentPackage pkg = installmentPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        pkg.setActive(false);
        installmentPackageRepository.save(pkg);
    }

    @Override
    public InstallmentPreviewResponse calculateInstallmentPreview(InstallmentCalculateRequest request) {
        InstallmentPackage pkg = installmentPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        double orderAmount = request.getOrderAmount();
        double downPaymentPercentage = pkg.getDownPaymentPercentage();
        double downPaymentAmount = orderAmount * (downPaymentPercentage / 100.0);
        double remainingBalance = orderAmount - downPaymentAmount;
        
        double interestRatePerMonth = (pkg.getInterestRate() / 100.0) / 12.0;
        int durationMonths = pkg.getDurationMonths();

        // Monthly payment using amortization formula: P * [r(1+r)^n] / [(1+r)^n – 1]
        double monthlyPayment;
        if (interestRatePerMonth > 0) {
            monthlyPayment = (remainingBalance * interestRatePerMonth * Math.pow(1 + interestRatePerMonth, durationMonths))
                    / (Math.pow(1 + interestRatePerMonth, durationMonths) - 1);
        } else {
            monthlyPayment = remainingBalance / durationMonths;
        }

        // Round to 2 decimal places
        monthlyPayment = Math.round(monthlyPayment * 100.0) / 100.0;
        double totalPayableAmount = downPaymentAmount + (monthlyPayment * durationMonths);

        List<PaymentScheduleResponse> schedule = new ArrayList<>();
        LocalDate buyDate = LocalDate.now();

        for (int i = 1; i <= durationMonths; i++) {
            // Requirement 6 & 7: First installment exactly 1 month after purchase.
            // LocalDate.plusMonths(1) handles the 31st vs 30th/28th edge case natively.
            LocalDate dueDate = buyDate.plusMonths(i);
            
            schedule.add(PaymentScheduleResponse.builder()
                    .installmentNo(i)
                    .amount(monthlyPayment)
                    .dueDate(dueDate)
                    .build());
        }

        return InstallmentPreviewResponse.builder()
                .orderAmount(orderAmount)
                .downPaymentPercentage(downPaymentPercentage)
                .downPaymentAmount(Math.round(downPaymentAmount * 100.0) / 100.0)
                .remainingBalance(Math.round(remainingBalance * 100.0) / 100.0)
                .monthlyInstallmentAmount(monthlyPayment)
                .interestRate(pkg.getInterestRate())
                .durationMonths(durationMonths)
                .totalPayableAmount(Math.round(totalPayableAmount * 100.0) / 100.0)
                .annualPenaltyRate(pkg.getAnnualPenaltyRate())
                .schedule(schedule)
                .build();
    }

    private InstallmentPackageResponse toResponse(InstallmentPackage pkg) {
        return InstallmentPackageResponse.builder()
                .packageId(pkg.getPackageId())
                .name(pkg.getName())
                .durationMonths(pkg.getDurationMonths())
                .interestRate(pkg.getInterestRate())
                .minOrderAmount(pkg.getMinOrderAmount())
                .downPaymentPercentage(pkg.getDownPaymentPercentage())
                .isActive(pkg.isActive())
                .annualPenaltyRate(pkg.getAnnualPenaltyRate())
                .build();
    }
}
