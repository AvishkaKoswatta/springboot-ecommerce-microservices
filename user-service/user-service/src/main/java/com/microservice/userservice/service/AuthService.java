package com.microservice.userservice.service;

import com.microservice.userservice.dto.AuthResponse;
import com.microservice.userservice.dto.LoginRequest;
import com.microservice.userservice.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void verifyEmail(String token);
    void resendVerificationEmail(String email);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
