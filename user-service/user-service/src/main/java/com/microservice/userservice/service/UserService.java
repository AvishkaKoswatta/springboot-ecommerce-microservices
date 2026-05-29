package com.microservice.userservice.service;

import com.microservice.userservice.dto.UpdateProfileRequest;
import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.dto.PasswordDtos.ChangePasswordRequest;
import com.microservice.userservice.dto.AdminDtos.UpdateUserRolesRequest;
import com.microservice.userservice.dto.AdminDtos.UpdateUserStatusRequest;
import com.microservice.userservice.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    // User operations
    UserDto getCurrentUser(String email);
    UserDto getUserById(Long id);
    UserDto updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);

    // Admin operations
    PagedResponse<UserDto> getAllUsers(Pageable pageable);
    UserDto updateUserRoles(Long userId, UpdateUserRolesRequest request);
    UserDto updateUserStatus(Long userId, UpdateUserStatusRequest request);
    void deleteUser(Long userId);
}
