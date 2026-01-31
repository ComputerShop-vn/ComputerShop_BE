package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);

    List<UserResponse> getAllUsers();
    UserResponse getUserById(int id);
    UserResponse updateUser(int id, UserUpdateRequest request);
    void deleteUser(int id);
    UserResponse getMyProfile();
    UserResponse updateMyProfile(UserUpdateRequest request);
}