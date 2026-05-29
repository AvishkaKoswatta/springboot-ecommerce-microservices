package com.microservice.orderservice.client;

import com.microservice.orderservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<UserDto> getUserById(Long id) {
        log.warn("user-service unavailable for user id: {}", id);
        return ApiResponse.<UserDto>builder().success(false)
                .message("user-service unavailable").build();
    }
}
