package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.UserResponse;
import sp26.group3.computer.sba301_computershop.entity.User;
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "blogs", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(source = "role.name", target = "roleName")
    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "blogs", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
