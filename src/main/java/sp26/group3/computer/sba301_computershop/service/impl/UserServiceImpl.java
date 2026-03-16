package sp26.group3.computer.sba301_computershop.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
import sp26.group3.computer.sba301_computershop.dto.response.UserResponse;
import sp26.group3.computer.sba301_computershop.entity.Role;
import sp26.group3.computer.sba301_computershop.entity.User;
import sp26.group3.computer.sba301_computershop.exception.AppException;
import sp26.group3.computer.sba301_computershop.exception.ErrorCode;
import sp26.group3.computer.sba301_computershop.mapper.UserMapper;
import sp26.group3.computer.sba301_computershop.repository.RoleRepository;
import sp26.group3.computer.sba301_computershop.repository.UserRepository;
import sp26.group3.computer.sba301_computershop.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role userRole = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus("ACTIVE");
        user.setRole(userRole);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findByStatus("ACTIVE")
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public PagedResponse<UserResponse> getAllUsersPaged(Pageable pageable) {
        Page<UserResponse> page = userRepository.findByStatus("ACTIVE", pageable)
                .map(userMapper::toUserResponse);
        return PagedResponse.<UserResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public UserResponse getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUser(int id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("===== DEBUG UPDATE USER =====");
        log.info("User ID: {}", id);
        log.info("Old status BEFORE mapper: {}", user.getStatus());
        log.info("Request status: {}", request.getStatus());

        userMapper.updateUser(user, request);

        log.info("Status AFTER mapper: {}", user.getStatus());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);

        log.info("Status AFTER save: {}", savedUser.getStatus());
        log.info("===== END DEBUG =====");

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public void deleteUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    @Override
    public UserResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logged in email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("User ID: " + user.getUserId());
        log.info("Old status: " + user.getStatus());
        log.info("Request status: " + request.getStatus());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus());
        }
        log.info("After set status: " + user.getStatus());
        User savedUser = userRepository.save(user);
        log.info("Saved status in object: " + savedUser.getStatus());
        return userMapper.toUserResponse(savedUser);
    }
}
