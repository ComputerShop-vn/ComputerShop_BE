package sp26.group3.computer.sba301_computershop.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {

    ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, encoding = @Encoding(name = "product", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ApiResponse<ProductResponse> createProduct(
            @RequestPart("product") @Valid ProductCreationRequest request,
            @Parameter(description = "Product images") @RequestPart(value = "images", required = false) MultipartFile[] images) {
        ProductResponse result = productService.createProduct(request, images);

        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        List<ProductResponse> result = productService.filterProducts(categoryId, brandId, minPrice, maxPrice);

        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable int id) {
        ProductDetailResponse result = productService.getProductById(id);

        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> result = productService.searchProducts(keyword);

        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, encoding = @Encoding(name = "product", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable int id,
            @RequestPart("product") @Valid ProductUpdateRequest request,
            @Parameter(description = "Product images") @RequestPart(value = "images", required = false) MultipartFile[] images) {
        ProductResponse result = productService.updateProduct(id, request, images);

        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);

        return new ApiResponse<>();
    }
}
