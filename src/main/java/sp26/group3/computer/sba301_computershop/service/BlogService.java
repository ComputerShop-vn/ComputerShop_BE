package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;

import java.util.List;

public interface BlogService {

    BlogResponse createBlog(BlogCreationRequest request);

    BlogResponse updateBlog(int blogId, BlogUpdateRequest request);

    BlogResponse getBlogById(int blogId);

    List<BlogResponse> getAllBlogs();

    PagedResponse<BlogResponse> getAllBlogsPaged(Pageable pageable);

    List<BlogResponse> getBlogsByUserId(int userId);

    PagedResponse<BlogResponse> getBlogsByUserIdPaged(int userId, Pageable pageable);

    void deleteBlog(int blogId);
}
