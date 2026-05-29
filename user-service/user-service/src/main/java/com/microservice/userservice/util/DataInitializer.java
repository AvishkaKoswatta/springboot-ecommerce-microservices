package com.microservice.userservice.util;

import com.microservice.userservice.entity.Role;
import com.microservice.userservice.entity.RoleName;
import com.microservice.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
    }

    private void initRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
        log.info("Role initialisation complete.");
    }

    private String getDescription(RoleName roleName) {
        return switch (roleName) {
            case ROLE_USER      -> "Standard user with basic access";
            case ROLE_ADMIN     -> "Administrator with full system access";
            case ROLE_MODERATOR -> "Moderator with content management access";
        };
    }
}
