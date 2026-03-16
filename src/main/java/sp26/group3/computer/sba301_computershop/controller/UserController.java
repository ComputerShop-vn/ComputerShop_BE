package sp26.group3.computer.sba301_computershop.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.ApiResponse;
import sp26.group3.computer.sba301_computershop.dto.response.PagedResponse;
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

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<List<UserResponse>> getAllUsers() {
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getAllUsers());
        return apiResponse;
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<PagedResponse<UserResponse>> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ApiResponse<PagedResponse<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getAllUsersPaged(pageable));
        return apiResponse;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<UserResponse> getUserById(@PathVariable int id) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUserById(id));
        return apiResponse;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<UserResponse> updateUser(@PathVariable int id, @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    ApiResponse<Void> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return new ApiResponse<>();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    ApiResponse<UserResponse> getMyProfile() {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getMyProfile());
        return apiResponse;
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('MEMBER','STAFF','ADMIN')")
    ApiResponse<UserResponse> updateMyProfile(@RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyProfile(request));
        return apiResponse;
    }
}
