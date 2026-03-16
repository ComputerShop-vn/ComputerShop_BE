package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.RoleCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RoleUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.RoleResponse;
import sp26.group3.computer.sba301_computershop.entity.Role;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.RoleMapper;
import sp26.group3.computer.sba301_computershop.repository.RoleRepository;
import sp26.group3.computer.sba301_computershop.service.RoleService;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    RoleMapper roleMapper;

    // ================= CREATE =================
    @Override
    public RoleResponse createRole(RoleCreationRequest request) {

        // optional: check duplicate name
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Role role = roleMapper.toRole(request);
        Role savedRole = roleRepository.save(role);

        return roleMapper.toRoleResponse(savedRole);
    }

    // ================= READ =================
    @Override
    public RoleResponse getRoleById(int id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Override
    public PagedResponse<RoleResponse> getAllRolesPaged(Pageable pageable) {
        Page<Role> page = roleRepository.findAll(pageable);
        return PagedResponse.<RoleResponse>builder()
                .content(page.getContent().stream().map(roleMapper::toRoleResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ================= UPDATE =================
    @Override
    public RoleResponse updateRole(int id, RoleUpdateRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleMapper.updateRole(role, request);

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(updatedRole);
    }

    // ================= DELETE =================
    @Override
    public void deleteRole(int id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roleRepository.delete(role);
    }
}
