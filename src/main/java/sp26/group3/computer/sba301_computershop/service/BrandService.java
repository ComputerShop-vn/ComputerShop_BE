package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;

import java.util.List;

public interface BrandService {

    BrandResponse createBrand(BrandCreationRequest request, MultipartFile logo);

    BrandResponse updateBrand(int brandId, BrandUpdateRequest request, MultipartFile logo);

    BrandResponse getBrandById(int brandId);

    List<BrandResponse> getAllBrands();

    PagedResponse<BrandResponse> getAllBrandsPaged(Pageable pageable);

    void deleteBrand(int brandId);
}
