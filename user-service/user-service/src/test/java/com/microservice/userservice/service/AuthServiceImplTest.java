package com.microservice.userservice.service;

import com.microservice.userservice.config.ApplicationProperties;
import com.microservice.userservice.dto.LoginRequest;
import com.microservice.userservice.dto.RegisterRequest;
import com.microservice.userservice.dto.AuthResponse;
import com.microservice.userservice.entity.*;
import com.microservice.userservice.exception.*;
import com.microservice.userservice.mapper.UserMapper;
import com.microservice.userservice.repository.RoleRepository;
import com.microservice.userservice.repository.UserRepository;
import com.microservice.userservice.repository.VerificationTokenRepository;
import com.microservice.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;
    @Mock private ApplicationProperties appProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginRequest = LoginRequest.builder()
                .emailOrUsername("john@example.com")
                .password("Password@123")
                .build();

        userRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_USER)
                .build();

        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        testUser.addRole(userRole);
    }

    // ─────────────────────────────── Register ───────────────────────────────

    @Test
    @DisplayName("register() - should create user and return auth token")
    void register_success() {
        ApplicationProperties.Email emailProps = new ApplicationProperties.Email();
        when(appProperties.getEmail()).thenReturn(emailProps);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any())).thenReturn(new VerificationToken());
        UserDetails mockDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mockDetails);
        when(jwtService.generateToken(any(UserDetails.class), anyLong()))
                .thenReturn("mock_jwt_token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        when(userMapper.toDto(any(User.class))).thenReturn(null);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock_jwt_token");
        verify(emailService).sendEmailVerification(anyString(), anyString(), anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() - should throw when email already exists")
    void register_emailAlreadyExists_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("register() - should throw when username already taken")
    void register_usernameAlreadyTaken_throwsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");
    }

    // ─────────────────────────────── Login ──────────────────────────────────

    @Test
    @DisplayName("login() - should return auth token on valid credentials")
    void login_success() {
        when(userRepository.findByEmailOrUsername("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password@123", testUser.getPassword())).thenReturn(true);
        UserDetails mockDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mockDetails);
        when(jwtService.generateToken(any(UserDetails.class), anyLong()))
                .thenReturn("mock_jwt_token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        when(userMapper.toDto(any(User.class))).thenReturn(null);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("mock_jwt_token");
        verify(userRepository).resetFailedLoginAttempts(testUser.getId());
    }

    @Test
    @DisplayName("login() - should throw on wrong password")
    void login_wrongPassword_throwsException() {
        when(userRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
        verify(userRepository).incrementFailedLoginAttempts(testUser.getId());
    }

    @Test
    @DisplayName("login() - should throw when email not verified")
    void login_emailNotVerified_throwsException() {
        testUser.setEmailVerified(false);
        when(userRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    @DisplayName("login() - should throw when user not found")
    void login_userNotFound_throwsException() {
        when(userRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ─────────────────────────── Forgot Password ────────────────────────────

    @Test
    @DisplayName("forgotPassword() - should send reset email when user exists")
    void forgotPassword_userExists_sendsEmail() {
        ApplicationProperties.Email emailProps = new ApplicationProperties.Email();
        when(appProperties.getEmail()).thenReturn(emailProps);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).invalidateExistingTokens(anyLong(), any(TokenType.class));
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword("john@example.com");

        verify(tokenRepository).invalidateExistingTokens(testUser.getId(), TokenType.PASSWORD_RESET);
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendPasswordResetEmail(
                eq("john@example.com"), eq("johndoe"), anyString());
    }

    @Test
    @DisplayName("forgotPassword() - should silently succeed when email not found (prevents enumeration)")
    void forgotPassword_userNotFound_silentSuccess() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.forgotPassword("unknown@example.com"))
                .doesNotThrowAnyException();

        verifyNoInteractions(tokenRepository, emailService);
    }

    // ─────────────────────────── Reset Password ─────────────────────────────

    @Test
    @DisplayName("resetPassword() - should update password and notify user")
    void resetPassword_success() {
        VerificationToken resetToken = VerificationToken.builder()
                .token("valid-token")
                .user(testUser)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        when(tokenRepository.findByTokenAndTokenType("valid-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.matches("NewPass@456", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("new_encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> authService.resetPassword("valid-token", "NewPass@456"))
                .doesNotThrowAnyException();

        verify(userRepository).save(testUser);
        assertThat(resetToken.getUsed()).isTrue();
        verify(emailService).sendPasswordChangedNotification(
                eq("john@example.com"), eq("johndoe"));
    }

    @Test
    @DisplayName("resetPassword() - should throw when token is invalid/not found")
    void resetPassword_invalidToken_throws() {
        when(tokenRepository.findByTokenAndTokenType(anyString(), eq(TokenType.PASSWORD_RESET)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "NewPass@456"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    @DisplayName("resetPassword() - should throw when token already used")
    void resetPassword_alreadyUsedToken_throws() {
        VerificationToken usedToken = VerificationToken.builder()
                .token("used-token")
                .user(testUser)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(30))
                .used(true)
                .build();

        when(tokenRepository.findByTokenAndTokenType("used-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> authService.resetPassword("used-token", "NewPass@456"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    @DisplayName("resetPassword() - should throw when token is expired")
    void resetPassword_expiredToken_throws() {
        VerificationToken expiredToken = VerificationToken.builder()
                .token("expired-token")
                .user(testUser)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(java.time.LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenAndTokenType("expired-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword("expired-token", "NewPass@456"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("resetPassword() - should throw when new password equals current password")
    void resetPassword_sameAsCurrentPassword_throws() {
        VerificationToken resetToken = VerificationToken.builder()
                .token("valid-token")
                .user(testUser)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        when(tokenRepository.findByTokenAndTokenType("valid-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(resetToken));
        // New password matches the stored (current) password
        when(passwordEncoder.matches("SamePass@123", testUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword("valid-token", "SamePass@123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different from your current password");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }
}
