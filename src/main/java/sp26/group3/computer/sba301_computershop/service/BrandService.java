package sp26.group3.computer.sba301_computershop.service;

import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {

    BrandResponse createBrand(BrandCreationRequest request, MultipartFile logo);

    BrandResponse updateBrand(int brandId, BrandUpdateRequest request, MultipartFile logo);

    BrandResponse getBrandById(int brandId);

    List<BrandResponse> getAllBrands();

    void deleteBrand(int brandId);
}
