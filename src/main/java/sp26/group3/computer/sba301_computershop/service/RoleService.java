package sp26.group3.computer.sba301_computershop.service;

import org.springframework.data.domain.Pageable;
import sp26.group3.computer.sba301_computershop.dto.request.RoleCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RoleUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    // CREATE
    RoleResponse createRole(RoleCreationRequest request);

    // READ
    RoleResponse getRoleById(int id);

    List<RoleResponse> getAllRoles();

    PagedResponse<RoleResponse> getAllRolesPaged(Pageable pageable);

    // UPDATE
    RoleResponse updateRole(int id, RoleUpdateRequest request);

    // DELETE
    void deleteRole(int id);
}
