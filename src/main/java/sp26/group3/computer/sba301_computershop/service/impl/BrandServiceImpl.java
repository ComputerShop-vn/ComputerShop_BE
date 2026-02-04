package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.entity.Brand;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.BrandMapper;
import sp26.group3.computer.sba301_computershop.repository.BrandRepository;
import sp26.group3.computer.sba301_computershop.service.BrandService;
import sp26.group3.computer.sba301_computershop.service.CloudinaryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BrandServiceImpl implements BrandService {

    BrandRepository brandRepository;
    BrandMapper brandMapper;
    CloudinaryService cloudinaryService;

    @Override
    public BrandResponse createBrand(BrandCreationRequest request, MultipartFile logo) {
        log.info("Creating brand with name: {}", request.getBrandName());

        // Check if brand name already exists
        if (brandRepository.existsByBrandName(request.getBrandName())) {
            throw new AppException(ErrorCode.BRAND_NAME_EXISTED);
        }

        Brand brand = brandMapper.toBrand(request);

        // Upload logo if provided
        if (logo != null && !logo.isEmpty()) {
            String logoUrl = cloudinaryService.uploadImage(logo);
            brand.setLogoUrl(logoUrl);
        }

        Brand savedBrand = brandRepository.save(brand);
        log.info("Brand created successfully with id: {}", savedBrand.getBrandId());

        return brandMapper.toBrandResponse(savedBrand);
    }

    @Override
    public BrandResponse updateBrand(int brandId, BrandUpdateRequest request, MultipartFile logo) {
        log.info("Updating brand with id: {}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        // Check if brand name is being changed and if it already exists
        if (request.getBrandName() != null &&
                !request.getBrandName().equals(brand.getBrandName()) &&
                brandRepository.existsByBrandName(request.getBrandName())) {
            throw new AppException(ErrorCode.BRAND_NAME_EXISTED);
        }

        brandMapper.updateBrand(brand, request);

        // Upload new logo if provided
        if (logo != null && !logo.isEmpty()) {
            String logoUrl = cloudinaryService.uploadImage(logo);
            brand.setLogoUrl(logoUrl);
        }

        Brand updatedBrand = brandRepository.save(brand);
        log.info("Brand updated successfully with id: {}", brandId);

        return brandMapper.toBrandResponse(updatedBrand);
    }

    @Override
    public BrandResponse getBrandById(int brandId) {
        log.info("Getting brand with id: {}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        return brandMapper.toBrandResponse(brand);
    }

    @Override
    public List<BrandResponse> getAllBrands() {
        log.info("Getting all brands");

        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    @Override
    public void deleteBrand(int brandId) {
        log.warn("Deleting brand with id: {}", brandId);

        if (!brandRepository.existsById(brandId)) {
            throw new AppException(ErrorCode.BRAND_NOT_FOUND);
        }

        brandRepository.deleteById(brandId);
        log.info("Brand deleted successfully with id: {}", brandId);
    }
}