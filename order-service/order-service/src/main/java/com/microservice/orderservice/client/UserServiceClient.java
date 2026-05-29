package com.microservice.orderservice.client;

import com.microservice.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        //url = "${services.user-service-url:}",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable Long id);

    record UserDto(Long id, String username, String email, String firstName, String lastName, String status) {}
}
