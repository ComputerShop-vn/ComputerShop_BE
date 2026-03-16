package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.CategoryRequest;
import sp26.group3.computer.sba301_computershop.dto.response.CategoryResponse;
import sp26.group3.computer.sba301_computershop.dto.response.CategoryResponse2;

import java.util.List;

public interface CategoryService {

    // ================= ADMIN CRUD =================
    CategoryResponse create(CategoryRequest request);

    List<CategoryResponse> getAll();

    CategoryResponse getById(int id);

    CategoryResponse update(int id, CategoryRequest request);

    void delete(int id);


    // ================= FRONTEND MENU =================

    // lấy category dạng tree (menu website)
    List<CategoryResponse2> getCategoryTree();

    // lấy tất cả parent category
    List<CategoryResponse2> getParentCategories();

    // lấy children của 1 category
    List<CategoryResponse2> getChildrenByParentId(int parentId);
}