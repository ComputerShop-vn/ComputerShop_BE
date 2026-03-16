package sp26.group3.computer.sba301_computershop.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
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
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.ProductUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductDetailResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.service.ProductService;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
            @Parameter(description = "Product images")
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(productService.createProduct(request, images));
        return response;
    }

    /**
     * Lọc sản phẩm theo category, brand, giá, và attribute.
     * Attribute filter formats:
     *   attributes=Socket:AM5              → Socket = AM5 (exact, case-insensitive)
     *   attributes=GPU Length:lte:400      → GPU Length ≤ 400 (numeric)
     *   attributes=Wattage:gte:750         → Wattage ≥ 750 (numeric)
     * Có thể dùng nhiều attributes cùng lúc: tất cả đều phải thỏa mãn trên cùng 1 variant.
     */
    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> attributes
    ) {
        // Parse "Socket:AM5" → {"Socket": "AM5"}
        Map<String, String> attrMap = new LinkedHashMap<>();
        if (attributes != null) {
            for (String attr : attributes) {
                int idx = attr.indexOf(':');
                if (idx > 0) {
                    attrMap.put(attr.substring(0, idx).trim(), attr.substring(idx + 1).trim());
                }
            }
        }
        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(productService.filterProducts(categoryId, brandId, minPrice, maxPrice, attrMap));
        return response;
    }

    @GetMapping("/paged")
    public ApiResponse<PagedResponse<ProductResponse>> getProductsPaged(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ApiResponse<PagedResponse<ProductResponse>> response = new ApiResponse<>();
        response.setResult(productService.filterProductsPaged(categoryId, brandId, minPrice, maxPrice, pageable));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable int id) {
        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setResult(productService.getProductById(id));
        return response;
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(productService.searchProducts(keyword));
        return response;
    }

    @GetMapping("/search/paged")
    public ApiResponse<PagedResponse<ProductResponse>> searchProductsPaged(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ApiResponse<PagedResponse<ProductResponse>> response = new ApiResponse<>();
        response.setResult(productService.searchProductsPaged(keyword, pageable));
        return response;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, encoding = @Encoding(name = "product", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable int id,
            @RequestPart("product") @Valid ProductUpdateRequest request,
            @Parameter(description = "Product images")
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(productService.updateProduct(id, request, images));
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return new ApiResponse<>();
    }
}
