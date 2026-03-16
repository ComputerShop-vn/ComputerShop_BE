package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.InstallmentPackageRequest;
import sp26.group3.computer.sba301_computershop.dto.response.InstallmentPackageResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import java.util.List;

public interface InstallmentPackageService {
    List<InstallmentPackageResponse> getActivePackages();

    List<InstallmentPackageResponse> getAllPackages();

    PagedResponse<InstallmentPackageResponse> getAllPackagesPaged(Pageable pageable);

    InstallmentPackageResponse createPackage(InstallmentPackageRequest request);

    InstallmentPackageResponse updatePackage(int id, InstallmentPackageRequest request);

    void deletePackage(int id);
}
