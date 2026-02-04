package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;

import java.util.List;

public interface BlogService {

    BlogResponse createBlog(BlogCreationRequest request);

    BlogResponse updateBlog(int blogId, BlogUpdateRequest request);

    BlogResponse getBlogById(int blogId);

    List<BlogResponse> getAllBlogs();

    List<BlogResponse> getBlogsByUserId(int userId);

    void deleteBlog(int blogId);
}