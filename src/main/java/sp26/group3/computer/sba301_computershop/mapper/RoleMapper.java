package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.RoleCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RoleUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.RoleResponse;
import sp26.group3.computer.sba301_computershop.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);

    void updateRole(@MappingTarget Role role, RoleUpdateRequest request);
}
