package sp26.group3.computer.sba301_computershop.service;

import sp26.group3.computer.sba301_computershop.dto.request.UserCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.UserUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateUser(Integer userId, UserUpdateRequest request);

    UserResponse getUserById(Integer userId);

    List<UserResponse> getAllUsers();

    void deleteUser(Integer userId);
}