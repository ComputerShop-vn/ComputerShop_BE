package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.entity.Brand;
import sp26.group3.computer.sba301_computershop.mapper.BrandMapper;
import sp26.group3.computer.sba301_computershop.repository.BrandRepository;
import sp26.group3.computer.sba301_computershop.service.BrandService;
import sp26.group3.computer.sba301_computershop.service.CloudinaryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public BrandResponse createBrand(BrandCreationRequest request, MultipartFile logo) {
        Brand brand = brandMapper.toBrand(request);

        if (logo != null && !logo.isEmpty()) {
            String logoUrl = cloudinaryService.uploadImage(logo);
            brand.setLogoUrl(logoUrl);
        }

        return brandMapper.toBrandResponse(
                brandRepository.save(brand)
        );
    }

    @Override
    public BrandResponse updateBrand(int brandId, BrandUpdateRequest request, MultipartFile logo) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        brandMapper.updateBrand(brand, request);

        if (logo != null && !logo.isEmpty()) {
            String logoUrl = cloudinaryService.uploadImage(logo);
            brand.setLogoUrl(logoUrl);
        }

        return brandMapper.toBrandResponse(
                brandRepository.save(brand)
        );
    }
    
    @Override
    public BrandResponse getBrandById(int brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        return brandMapper.toBrandResponse(brand);
    }


    @Override
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    @Override
    public void deleteBrand(int brandId) {
        brandRepository.deleteById(brandId);
    }
}
