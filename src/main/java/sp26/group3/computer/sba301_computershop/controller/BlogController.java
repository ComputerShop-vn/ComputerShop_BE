package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;
import sp26.group3.computer.sba301_computershop.service.BlogService;

import java.util.List;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogController {

    BlogService blogService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ApiResponse<BlogResponse> createBlog(@RequestBody @Valid BlogCreationRequest request) {
        log.info("[POST] /blogs - Create blog");

        BlogResponse result = blogService.createBlog(request);

        log.info("[POST] /blogs - SUCCESS | blogId={}", result.getBlogId());

        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ ALL =================
    @GetMapping
    public ApiResponse<List<BlogResponse>> getAllBlogs() {
        log.info("[GET] /blogs - Get all blogs");

        List<BlogResponse> result = blogService.getAllBlogs();

        log.info("[GET] /blogs - Total blogs={}", result.size());

        ApiResponse<List<BlogResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ApiResponse<BlogResponse> getBlogById(@PathVariable int id) {
        log.info("[GET] /blogs/{} - Get blog by id", id);

        BlogResponse result = blogService.getBlogById(id);

        log.info("[GET] /blogs/{} - SUCCESS | title={}", id, result.getTitle());

        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= READ BY USER ID =================
    @GetMapping("/user/{userId}")
    public ApiResponse<List<BlogResponse>> getBlogsByUserId(@PathVariable int userId) {
        log.info("[GET] /blogs/user/{} - Get blogs by user id", userId);

        List<BlogResponse> result = blogService.getBlogsByUserId(userId);

        log.info("[GET] /blogs/user/{} - Total blogs={}", userId, result.size());

        ApiResponse<List<BlogResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ApiResponse<BlogResponse> updateBlog(
            @PathVariable int id,
            @RequestBody @Valid BlogUpdateRequest request
    ) {
        log.info("[PUT] /blogs/{} - Update blog", id);

        BlogResponse result = blogService.updateBlog(id, request);

        log.info("[PUT] /blogs/{} - Update SUCCESS", id);

        ApiResponse<BlogResponse> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<Void> deleteBlog(@PathVariable int id) {
        log.warn("[DELETE] /blogs/{} - Delete blog", id);

        blogService.deleteBlog(id);

        log.warn("[DELETE] /blogs/{} - SUCCESS", id);

        return new ApiResponse<>();
    }
}