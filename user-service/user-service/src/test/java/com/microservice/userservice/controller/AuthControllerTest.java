package com.microservice.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.userservice.dto.AuthResponse;
import com.microservice.userservice.dto.LoginRequest;
import com.microservice.userservice.dto.RegisterRequest;
import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.exception.BadCredentialsException;
import com.microservice.userservice.exception.UserAlreadyExistsException;
import com.microservice.userservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    @Test
    @DisplayName("POST /auth/register - should return 201 on valid request")
    @WithMockUser
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("mock_token")
                .expiresIn(86400000L)
                .user(UserDto.builder().email("john@example.com").build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock_token"));
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 on invalid email")
    @WithMockUser
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .email("not-an-email")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/register - should return 409 when email taken")
    @WithMockUser
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(authService.register(any())).thenThrow(new UserAlreadyExistsException("Email already in use"));

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/login - should return 200 on valid credentials")
    @WithMockUser
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");

        AuthResponse response = AuthResponse.builder()
                .accessToken("mock_token")
                .expiresIn(86400000L)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("mock_token"));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 on bad credentials")
    @WithMockUser
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPass@1");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
