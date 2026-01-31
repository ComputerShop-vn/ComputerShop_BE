package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.UserResponse;
import sp26.group3.computer.sba301_computershop.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    // ================= CREATE =================
    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("[POST] /users - Create user | email={}", request.getEmail());

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));

        log.info("[POST] /users - Create user SUCCESS | userId={}",
                apiResponse.getResult().getUserId());
        return apiResponse;
    }

    // ================= READ ALL =================
    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        log.info("[GET] /users - Get all users");

        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getAllUsers());

        log.info("[GET] /users - Total users={}", apiResponse.getResult().size());
        return apiResponse;
    }

    // ================= READ BY ID =================
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable int id) {
        log.info("[GET] /users/{} - Get user by id", id);

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUserById(id));

        log.info("[GET] /users/{} - SUCCESS | username={}",
                id, apiResponse.getResult().getUsername());
        return apiResponse;
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    ApiResponse<UserResponse> updateUser(
            @PathVariable int id,
            @RequestBody @Valid UserUpdateRequest request) {

        log.info("[PUT] /users/{} - Update user", id);

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(id, request));

        log.info("[PUT] /users/{} - Update SUCCESS", id);
        return apiResponse;
    }

    // ================= DELETE (SOFT DELETE) =================
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteUser(@PathVariable int id) {
        log.warn("[DELETE] /users/{} - Soft delete user", id);

        userService.deleteUser(id);

        log.warn("[DELETE] /users/{} - SUCCESS", id);
        return new ApiResponse<>();
    }

    // ================= SELF PROFILE =================
    @GetMapping("/me")
    ApiResponse<UserResponse> getMyProfile() {
        log.info("[GET] /users/me - Get my profile");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getMyProfile());

        log.info("[GET] /users/me - SUCCESS | username={}",
                apiResponse.getResult().getUsername());
        return apiResponse;
    }

    @PutMapping("/me")
    ApiResponse<UserResponse> updateMyProfile(
            @RequestBody @Valid UserUpdateRequest request) {

        log.info("[PUT] /users/me - Update my profile");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyProfile(request));

        log.info("[PUT] /users/me - Update SUCCESS");
        return apiResponse;
    }
}
