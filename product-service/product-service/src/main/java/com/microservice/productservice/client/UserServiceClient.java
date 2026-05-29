package com.microservice.productservice.client;

import com.microservice.productservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to validate seller/user existence via user-service.
 * Resolved via Eureka using the service name "user-service".
 */
@FeignClient(
        name = "user-service",
        //url = "${services.user-service-url:}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable Long id);

    /**
     * Minimal DTO — only fields product-service cares about.
     */
    record UserDto(Long id, String username, String email, String status) {}
}
