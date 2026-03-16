package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.service.BlogService;

import java.util.List;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogController {

    BlogService blogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<BlogResponse> createBlog(@RequestBody @Valid BlogCreationRequest request) {
        BlogResponse result = blogService.createBlog(request);
        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping
    public ApiResponse<List<BlogResponse>> getAllBlogs() {
        ApiResponse<List<BlogResponse>> response = new ApiResponse<>();
        response.setResult(blogService.getAllBlogs());
        return response;
    }

    @GetMapping("/paged")
    public ApiResponse<PagedResponse<BlogResponse>> getAllBlogsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "blogId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ApiResponse<PagedResponse<BlogResponse>> response = new ApiResponse<>();
        response.setResult(blogService.getAllBlogsPaged(pageable));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogResponse> getBlogById(@PathVariable int id) {
        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(blogService.getBlogById(id));
        return response;
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<BlogResponse>> getBlogsByUserId(@PathVariable int userId) {
        ApiResponse<List<BlogResponse>> response = new ApiResponse<>();
        response.setResult(blogService.getBlogsByUserId(userId));
        return response;
    }

    @GetMapping("/user/{userId}/paged")
    public ApiResponse<PagedResponse<BlogResponse>> getBlogsByUserIdPaged(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("blogId").descending());
        ApiResponse<PagedResponse<BlogResponse>> response = new ApiResponse<>();
        response.setResult(blogService.getBlogsByUserIdPaged(userId, pageable));
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    public ApiResponse<BlogResponse> updateBlog(@PathVariable int id, @RequestBody @Valid BlogUpdateRequest request) {
        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(blogService.updateBlog(id, request));
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteBlog(@PathVariable int id) {
        blogService.deleteBlog(id);
        return new ApiResponse<>();
    }
}
