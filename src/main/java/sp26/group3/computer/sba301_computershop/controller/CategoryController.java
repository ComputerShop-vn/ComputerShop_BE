package sp26.group3.computer.sba301_computershop.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.CategoryResponse;
import sp26.group3.computer.sba301_computershop.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing category")
public class CategoryController {

    CategoryService categoryService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest request) {

        log.info("[POST] /categories - Create category | name={}", request.getCategoryName());

        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.create(request));

        log.info("[POST] /categories - Create SUCCESS | categoryId={}",
                apiResponse.getResult().getCategoryId());
        return apiResponse;
    }

    // ================= READ ALL =================
    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {

        log.info("[GET] /categories - Get all categories");

        ApiResponse<List<CategoryResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getAll());

        log.info("[GET] /categories - Total categories={}",
                apiResponse.getResult().size());
        return apiResponse;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> getCategoryById(@PathVariable int id) {

        log.info("[GET] /categories/{} - Get category by id", id);

        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getById(id));

        log.info("[GET] /categories/{} - SUCCESS | name={}",
                id, apiResponse.getResult().getCategoryName());
        return apiResponse;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<CategoryResponse> updateCategory(
            @PathVariable int id,
            @RequestBody @Valid CategoryRequest request) {

        log.info("[PUT] /categories/{} - Update category", id);

        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.update(id, request));

        log.info("[PUT] /categories/{} - Update SUCCESS", id);
        return apiResponse;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<Void> deleteCategory(@PathVariable int id) {

        log.warn("[DELETE] /categories/{} - Delete category", id);

        categoryService.delete(id);

        log.warn("[DELETE] /categories/{} - SUCCESS", id);
        return new ApiResponse<>();
    }
}
