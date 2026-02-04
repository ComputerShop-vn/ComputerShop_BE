package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.service.BrandService;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BrandController {

    BrandService brandService;

    // ================= CREATE =================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<BrandResponse> createBrand(
            @RequestPart("data") @Valid BrandCreationRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {

        log.info("[POST] /brands - Create brand | name={}", request.getBrandName());

        BrandResponse result = brandService.createBrand(request, logo);

        log.info("[POST] /brands - Create SUCCESS | brandId={}", result.getBrandId());

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
            @RequestPart("data") @Valid BrandUpdateRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {

        log.info("[PUT] /brands/{} - Update brand", id);

        BrandResponse result = brandService.updateBrand(id, request, logo);

        log.info("[PUT] /brands/{} - Update SUCCESS", id);

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


