package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CategoryResponse;
import sp26.group3.computer.sba301_computershop.entity.Category;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.CategoryMapper;
import sp26.group3.computer.sba301_computershop.repository.CategoryRepository;
import sp26.group3.computer.sba301_computershop.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        log.info("Create category | name={}", request.getCategoryName());

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParentCategory(parent);
        }

        return categoryMapper.toCategoryResponse(
                categoryRepository.save(category)
        );
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse update(int id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);

        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId() == category.getCategoryId()) {
                throw new IllegalArgumentException("Category cannot be parent of itself");
            }

            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        return categoryMapper.toCategoryResponse(
                categoryRepository.save(category)
        );
    }

    @Override
    public void delete(int id) {
        // Prevent deletion if this category has child categories
        boolean hasChildren = categoryRepository.findAll()
                .stream()
                .anyMatch(c -> c.getParentCategory() != null && c.getParentCategory().getCategoryId() == id);

        if (hasChildren) {
            throw new IllegalStateException("Cannot delete category with child categories");
        }

        categoryRepository.deleteById(id);
    }
}
