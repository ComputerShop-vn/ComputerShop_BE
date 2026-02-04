package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;
import sp26.group3.computer.sba301_computershop.entity.Blog;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.BlogMapper;
import sp26.group3.computer.sba301_computershop.repository.BlogRepository;
import sp26.group3.computer.sba301_computershop.repository.UserRepository;
import sp26.group3.computer.sba301_computershop.service.BlogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogServiceImpl implements BlogService {

    BlogRepository blogRepository;
    UserRepository userRepository;
    BlogMapper blogMapper;

    @Override
    public BlogResponse createBlog(BlogCreationRequest request) {
        log.info("Creating blog with title: {}", request.getTitle());

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Blog blog = blogMapper.toBlog(request);
        blog.setUser(user);
        blog.setPublishedAt(LocalDateTime.now());

        Blog savedBlog = blogRepository.save(blog);
        log.info("Blog created successfully with id: {}", savedBlog.getBlogId());

        return blogMapper.toBlogResponse(savedBlog);
    }

    @Override
    public BlogResponse updateBlog(int blogId, BlogUpdateRequest request) {
        log.info("Updating blog with id: {}", blogId);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blogMapper.updateBlog(blog, request);

        Blog updatedBlog = blogRepository.save(blog);
        log.info("Blog updated successfully with id: {}", blogId);

        return blogMapper.toBlogResponse(updatedBlog);
    }

    @Override
    public BlogResponse getBlogById(int blogId) {
        log.info("Getting blog with id: {}", blogId);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    public List<BlogResponse> getAllBlogs() {
        log.info("Getting all blogs");

        return blogRepository.findAll()
                .stream()
                .map(blogMapper::toBlogResponse)
                .toList();
    }

    @Override
    public List<BlogResponse> getBlogsByUserId(int userId) {
        log.info("Getting blogs for user id: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        return blogRepository.findByUser_UserId(userId)
                .stream()
                .map(blogMapper::toBlogResponse)
                .toList();
    }

    @Override
    public void deleteBlog(int blogId) {
        log.warn("Deleting blog with id: {}", blogId);

        if (!blogRepository.existsById(blogId)) {
            throw new AppException(ErrorCode.BLOG_NOT_FOUND);
        }

        blogRepository.deleteById(blogId);
        log.info("Blog deleted successfully with id: {}", blogId);
    }
}