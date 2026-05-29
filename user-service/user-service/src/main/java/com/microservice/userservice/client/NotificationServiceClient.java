package com.microservice.userservice.client;

import com.microservice.userservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Example Feign client for calling another microservice (e.g. notification-service).
 * Replace the name and url with the actual service registered in your service registry
 * (Eureka, Consul, etc.) or provide a direct URL for local development.
 *
 * Usage:
 *   - With Eureka:  @FeignClient(name = "notification-service")
 *   - With URL:     @FeignClient(name = "notification-service", url = "${services.notification-url}")
 */
@FeignClient(
        name = "notification-service"
        //url = "${services.notification-url:http://localhost:8082}"
)
public interface NotificationServiceClient {

    @GetMapping("/notifications/user/{userId}/unread-count")
    ApiResponse<Long> getUnreadNotificationCount(@PathVariable Long userId);
}
