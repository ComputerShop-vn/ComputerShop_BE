package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.RoleCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RoleUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.RoleResponse;
import sp26.group3.computer.sba301_computershop.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {

    RoleService roleService;

    // ================= CREATE =================
    @PostMapping
    ApiResponse<RoleResponse> createRole(
            @RequestBody @Valid RoleCreationRequest request) {

        log.info("[POST] /roles - Create role | name={}", request.getName());

        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.setResult(roleService.createRole(request));

        log.info("[POST] /roles - Create role SUCCESS | roleId={}",
                response.getResult().getRoleId());

        return response;
    }

    // ================= READ =================
    @GetMapping("/{id}")
    ApiResponse<RoleResponse> getRoleById(@PathVariable int id) {

        log.info("[GET] /roles/{} - Get role by id", id);

        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.setResult(roleService.getRoleById(id));

        log.info("[GET] /roles/{} - Get role SUCCESS", id);

        return response;
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAllRoles() {

        log.info("[GET] /roles - Get all roles");

        ApiResponse<List<RoleResponse>> response = new ApiResponse<>();
        response.setResult(roleService.getAllRoles());

        log.info("[GET] /roles - Get all roles SUCCESS | size={}",
                response.getResult().size());

        return response;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    ApiResponse<RoleResponse> updateRole(
            @PathVariable int id,
            @RequestBody @Valid RoleUpdateRequest request) {

        log.info("[PUT] /roles/{} - Update role", id);

        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.setResult(roleService.updateRole(id, request));

        log.info("[PUT] /roles/{} - Update role SUCCESS", id);

        return response;
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteRole(@PathVariable int id) {

        log.info("[DELETE] /roles/{} - Delete role", id);

        roleService.deleteRole(id);

        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Role deleted successfully");

        log.info("[DELETE] /roles/{} - Delete role SUCCESS", id);

        return response;
    }
}
