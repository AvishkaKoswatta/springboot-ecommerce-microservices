package com.microservice.userservice.service;

import com.microservice.userservice.dto.AdminDtos.UpdateUserRolesRequest;
import com.microservice.userservice.dto.AdminDtos.UpdateUserStatusRequest;
import com.microservice.userservice.dto.PasswordDtos.ChangePasswordRequest;
import com.microservice.userservice.dto.UpdateProfileRequest;
import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.entity.Role;
import com.microservice.userservice.entity.RoleName;
import com.microservice.userservice.entity.User;
import com.microservice.userservice.exception.BadCredentialsException;
import com.microservice.userservice.exception.RoleNotFoundException;
import com.microservice.userservice.exception.UserAlreadyExistsException;
import com.microservice.userservice.exception.UserNotFoundException;
import com.microservice.userservice.mapper.UserMapper;
import com.microservice.userservice.repository.RoleRepository;
import com.microservice.userservice.repository.UserRepository;
import com.microservice.userservice.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override //This method is implementing a method defined in UserService interface
    @Transactional(readOnly = true) //If error happens everything is rolled back
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return userMapper.toDto(user);
    }
    // find email using userRepository and findByEmail, store it in user variable of User data type
    // but return safe version of User data type which is UserDto

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        // Username uniqueness check
        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName()))  user.setLastName(request.getLastName());
        if (StringUtils.hasText(request.getPhoneNumber())) user.setPhoneNumber(request.getPhoneNumber());
        if (StringUtils.hasText(request.getProfilePictureUrl())) user.setProfilePictureUrl(request.getProfilePictureUrl());

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
        log.info("Password changed for user: {}", email);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin operations
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(Pageable pageable) {
        Page<UserDto> page = userRepository.findAll(pageable).map(userMapper::toDto);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Set<Role> newRoles = new HashSet<>(); //Create empty collection
        for (RoleName roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        user = userRepository.save(user);
        log.info("Roles updated for user ID {}: {}", userId, request.getRoles());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setStatus(request.getStatus());
        user = userRepository.save(user);
        log.info("Status updated for user ID {} to {}", userId, request.getStatus());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
        log.info("User deleted with ID: {}", userId);
    }
}
