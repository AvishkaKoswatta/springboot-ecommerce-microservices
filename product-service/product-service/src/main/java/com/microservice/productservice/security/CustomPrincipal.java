package com.microservice.productservice.security;

import java.io.Serializable;

public record CustomPrincipal(
        Long userId,
        String username
) implements Serializable {
}