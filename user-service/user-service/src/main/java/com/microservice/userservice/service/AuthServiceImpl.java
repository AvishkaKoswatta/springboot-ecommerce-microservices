package com.microservice.userservice.service;

import com.microservice.userservice.config.ApplicationProperties;
import com.microservice.userservice.dto.AuthResponse;
import com.microservice.userservice.dto.LoginRequest;
import com.microservice.userservice.dto.RegisterRequest;
import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.entity.*;
import com.microservice.userservice.exception.*;
import com.microservice.userservice.mapper.UserMapper;
import com.microservice.userservice.repository.RoleRepository;
import com.microservice.userservice.repository.UserRepository;
import com.microservice.userservice.repository.VerificationTokenRepository;
import com.microservice.userservice.security.JwtService;
import com.microservice.userservice.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final ApplicationProperties appProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for existing email / username
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        // Fetch default USER role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RoleNotFoundException("Default role ROLE_USER not found. Please initialise roles."));

        // Build and save user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();
        user.addRole(userRole);
        user = userRepository.save(user);

        // Create verification token and send email
        VerificationToken verificationToken = VerificationToken.createEmailVerificationToken(
                user,
                appProperties.getEmail().getVerificationExpiryHours()
        );
        tokenRepository.save(verificationToken);
        emailService.sendEmailVerification(user.getEmail(), user.getUsername(), verificationToken.getToken());

        log.info("User registered successfully: {}", user.getEmail());

        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        //String jwt = jwtService.generateToken(userDetails);
        String jwt = jwtService.generateToken(userDetails, user.getId());
        UserDto userDto = userMapper.toDto(user);

        return AuthResponse.builder()
                .accessToken(jwt)
                .expiresIn(jwtService.getExpirationTime())
                .user(userDto)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrUsername(request.getEmailOrUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid email/username or password"));

        // Account lock check
        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "Account is locked until " + user.getLockedUntil() + ". Too many failed login attempts.");
        }

        // Password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid email/username or password");
        }

        // Email verification check
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in.");
        }

        // Status check
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.INACTIVE) {
            throw new AccountLockedException("Your account has been " + user.getStatus().name().toLowerCase() + ".");
        }

        // Reset failed attempts on success
        userRepository.resetFailedLoginAttempts(user.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        //String jwt = jwtService.generateToken(userDetails);
        String jwt = jwtService.generateToken(userDetails, user.getId());
        UserDto userDto = userMapper.toDto(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(jwt)
                .expiresIn(jwtService.getExpirationTime())
                .user(userDto)
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository
                .findByTokenAndTokenType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        if (verificationToken.getUsed()) {
            throw new InvalidTokenException("This verification link has already been used");
        }
        if (verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Invalidate existing tokens
        tokenRepository.invalidateExistingTokens(user.getId(), TokenType.EMAIL_VERIFICATION);

        VerificationToken newToken = VerificationToken.createEmailVerificationToken(
                user,
                appProperties.getEmail().getVerificationExpiryHours()
        );
        tokenRepository.save(newToken);
        emailService.sendEmailVerification(user.getEmail(), user.getUsername(), newToken.getToken());

        log.info("Verification email resent to: {}", email);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null); // Return silently to prevent email enumeration

        if (user != null) {
            tokenRepository.invalidateExistingTokens(user.getId(), TokenType.PASSWORD_RESET);

            VerificationToken resetToken = VerificationToken.createPasswordResetToken(
                    user,
                    appProperties.getEmail().getPasswordResetExpiryMinutes()
            );
            tokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken.getToken());
            log.info("Password reset email sent to: {}", email);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken resetToken = tokenRepository
                .findByTokenAndTokenType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (resetToken.getUsed()) {
            throw new InvalidTokenException("This reset link has already been used");
        }
        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void handleFailedLogin(User user) {
        userRepository.incrementFailedLoginAttempts(user.getId());
        int attempts = user.getFailedLoginAttempts() + 1;

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            user.setStatus(UserStatus.LOCKED);
            userRepository.save(user);
            log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
        }
    }
}
