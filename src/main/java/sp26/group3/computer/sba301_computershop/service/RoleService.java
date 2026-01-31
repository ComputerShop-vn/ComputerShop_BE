package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.RoleCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RoleUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    // CREATE
    RoleResponse createRole(RoleCreationRequest request);

    // READ
    RoleResponse getRoleById(int id);

    List<RoleResponse> getAllRoles();

    // UPDATE
    RoleResponse updateRole(int id, RoleUpdateRequest request);

    // DELETE
    void deleteRole(int id);
}
