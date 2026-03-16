package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.service.BrandService;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BrandController {

    BrandService brandService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<BrandResponse> createBrand(
            @RequestParam("brandName") @NotBlank String brandName,
            @RequestParam(value = "logo", required = false) MultipartFile logo
    ) {

        BrandCreationRequest request = BrandCreationRequest.builder()
                .brandName(brandName)
                .build();

        BrandResponse result = brandService.createBrand(request, logo);

        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ ALL =================
    @GetMapping
    public ApiResponse<List<BrandResponse>> getAllBrands() {
        log.info("[GET] /brands - Get all brands");

        List<BrandResponse> result = brandService.getAllBrands();

        log.info("[GET] /brands - Total brands={}", result.size());

        ApiResponse<List<BrandResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ ALL PAGED =================
    @GetMapping("/paged")
    public ApiResponse<PagedResponse<BrandResponse>> getAllBrandsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brandId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ApiResponse<PagedResponse<BrandResponse>> response = new ApiResponse<>();
        response.setResult(brandService.getAllBrandsPaged(pageable));
        return response;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> getBrandById(@PathVariable int id) {
        log.info("[GET] /brands/{} - Get brand by id", id);

        BrandResponse result = brandService.getBrandById(id);

        log.info("[GET] /brands/{} - SUCCESS | brandName={}", id, result.getBrandName());

        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= UPDATE =================
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable int id,
            @RequestParam("brandName") @NotBlank String brandName,
            @RequestParam(value = "logo", required = false) MultipartFile logo
    ) {
        BrandUpdateRequest request = BrandUpdateRequest.builder()
                .brandName(brandName)
                .build();

        BrandResponse result = brandService.updateBrand(id, request, logo);

        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable int id) {
        log.warn("[DELETE] /brands/{} - Delete brand", id);

        brandService.deleteBrand(id);

        log.warn("[DELETE] /brands/{} - SUCCESS", id);

        return new ApiResponse<>();
    }
}


