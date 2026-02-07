package sp26.group3.computer.sba301_computershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.ProductCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.ProductResponse;
import sp26.group3.computer.sba301_computershop.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Validated ProductCreationRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ApiResponse.<ProductResponse>builder()
                .result(response)
                .message("Product created successfully")
                .build();
    }
}
