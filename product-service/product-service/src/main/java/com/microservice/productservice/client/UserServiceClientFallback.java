package com.microservice.productservice.client;

import com.microservice.productservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback triggered when user-service is unavailable.
 * Product-service degrades gracefully — seller validation is skipped.
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<UserDto> getUserById(Long id) {
        log.warn("user-service unavailable; falling back for user ID: {}", id);
        return ApiResponse.<UserDto>builder()
                .success(false)
                .message("user-service unavailable")
                .build();
    }
}
