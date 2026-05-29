package com.microservice.userservice.service;

import com.microservice.userservice.dto.PasswordDtos.ChangePasswordRequest;
import com.microservice.userservice.dto.UpdateProfileRequest;
import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.entity.*;
import com.microservice.userservice.exception.*;
import com.microservice.userservice.mapper.UserMapper;
import com.microservice.userservice.repository.RoleRepository;
import com.microservice.userservice.repository.UserRepository;
import com.microservice.userservice.response.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleName.ROLE_USER).build();
        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("encoded_pass")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        testUser.addRole(role);

        testUserDto = UserDto.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    @DisplayName("getCurrentUser() - should return user dto for authenticated user")
    void getCurrentUser_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getCurrentUser("john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("getCurrentUser() - should throw when user not found")
    void getCurrentUser_notFound_throws() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile() - should update fields and return updated dto")
    void updateProfile_success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.updateProfile("john@example.com", request);

        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateProfile() - should throw when username already taken")
    void updateProfile_usernameTaken_throws() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username("taken_name")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("taken_name")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("john@example.com", request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("changePassword() - should succeed with correct current password")
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@123", "NewPass@456", "NewPass@456");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass@123", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.matches("NewPass@456", "encoded_pass")).thenReturn(false);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("new_encoded_pass");
        when(userRepository.save(any())).thenReturn(testUser);

        assertThatCode(() -> userService.changePassword("john@example.com", request))
                .doesNotThrowAnyException();
        verify(emailService).sendPasswordChangedNotification(anyString(), anyString());
    }

    @Test
    @DisplayName("changePassword() - should throw when passwords don't match")
    void changePassword_mismatch_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@123", "NewPass@456", "DifferentPass@789");

        assertThatThrownBy(() -> userService.changePassword("john@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("changePassword() - should throw on wrong current password")
    void changePassword_wrongCurrentPassword_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPass@123", "NewPass@456", "NewPass@456");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass@123", "encoded_pass")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john@example.com", request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("changePassword() - should throw when new password is the same as current")
    void changePassword_sameAsCurrentPassword_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@123", "OldPass@123", "OldPass@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass@123", "encoded_pass")).thenReturn(true); // current password matches
        when(passwordEncoder.matches("OldPass@123", "encoded_pass")).thenReturn(true); // reuse check also matches

        assertThatThrownBy(() -> userService.changePassword("john@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different from current");
    }

    @Test
    @DisplayName("getAllUsers() - should return paged list")
    void getAllUsers_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        PagedResponse<UserDto> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteUser() - should delete user by id")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        assertThatCode(() -> userService.deleteUser(1L)).doesNotThrowAnyException();
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser() - should throw when user not found")
    void deleteUser_notFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
