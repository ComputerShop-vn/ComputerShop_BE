package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.BlogCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BlogUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BlogResponse;
import sp26.group3.computer.sba301_computershop.entity.Blog;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Blog toBlog(BlogCreationRequest request);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    BlogResponse toBlogResponse(Blog blog);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void updateBlog(@MappingTarget Blog blog, BlogUpdateRequest request);
}