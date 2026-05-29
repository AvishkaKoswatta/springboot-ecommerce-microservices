package com.microservice.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // A valid 256-bit base64-encoded secret key for testing
    private static final String TEST_SECRET =
            "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        userDetails = User.builder()
                .username("john@example.com")
                .password("encoded_pass")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken() should produce a non-null token")
    void generateToken_returnsNonNull() {
        String token = jwtService.generateToken(userDetails, 1L);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("extractUsername() should return correct email from token")
    void extractUsername_returnsCorrectEmail() {
        String token = jwtService.generateToken(userDetails, 1L);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("isTokenValid() should return true for freshly generated token")
    void isTokenValid_freshToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails, 1L);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid() should return false for token with wrong user")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails, 1L);

        UserDetails anotherUser = User.builder()
                .username("other@example.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    @DisplayName("validateToken() should return true for valid token")
    void validateToken_validToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails, 1L);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken() should return false for malformed token")
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtService.validateToken("this.is.not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("getExpirationTime() should return configured value")
    void getExpirationTime_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationTime()).isEqualTo(86400000L);
    }
}
