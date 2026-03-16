package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.CategoryRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CategoryResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    List<CategoryResponse> getAll();
    PagedResponse<CategoryResponse> getAllPaged(Pageable pageable);
    CategoryResponse getById(int id);
    CategoryResponse update(int id, CategoryRequest request);
    void delete(int id);
}
