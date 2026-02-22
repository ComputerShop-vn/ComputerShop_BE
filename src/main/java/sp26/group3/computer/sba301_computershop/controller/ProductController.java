package sp26.group3.computer.sba301_computershop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Operation(summary = "Create new product", description = "Create a new product with images (Admin/Staff only)")
    public ApiResponse<ProductResponse> createProduct(
            @RequestPart("product") @Valid ProductCreationRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        ProductResponse result = productService.createProduct(request, images);

        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping
    @Operation(summary = "Get all products with optional filters", 
               description = "Retrieve all products. Supports filtering by category, brand, and price range via query parameters")
    public ApiResponse<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        List<ProductResponse> result = productService.filterProducts(categoryId, brandId, minPrice, maxPrice);

        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get full product details including images, reviews, promotions")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable int id) {
        ProductDetailResponse result = productService.getProductById(id);

        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword in name or description")
    public ApiResponse<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> result = productService.searchProducts(keyword);

        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Operation(summary = "Update product", description = "Update product details with optional new images (Admin/Staff only)")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable int id,
            @RequestPart("product") @Valid ProductUpdateRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        ProductResponse result = productService.updateProduct(id, request, images);

        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Operation(summary = "Delete product", description = "Delete a product (Admin/Staff only)")
    public ApiResponse<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);

        return new ApiResponse<>();
    }
}
