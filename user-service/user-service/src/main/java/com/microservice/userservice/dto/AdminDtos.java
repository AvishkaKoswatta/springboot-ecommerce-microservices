package com.microservice.userservice.dto;

import com.microservice.userservice.entity.RoleName;
import com.microservice.userservice.entity.UserStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

public class AdminDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateUserRolesRequest {
        @NotNull(message = "Roles are required")
        @NotEmpty(message = "At least one role is required")
        private Set<RoleName> roles;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateUserStatusRequest {
        @NotNull(message = "Status is required")
        private UserStatus status;
    }
}
