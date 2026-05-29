package com.microservice.userservice.mapper;

import com.microservice.userservice.dto.UserDto;
import com.microservice.userservice.entity.Role;
import com.microservice.userservice.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserDto toDto(User user);

    @Named("mapRoles")
    default Set<String> mapRoles(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
