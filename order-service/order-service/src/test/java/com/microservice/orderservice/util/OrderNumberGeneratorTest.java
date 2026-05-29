package com.microservice.orderservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNumberGeneratorTest {

    @Test
    @DisplayName("generate() - should return non-null, non-blank order number")
    void generate_notBlank() {
        String num = OrderNumberGenerator.generate();
        assertThat(num).isNotBlank();
    }

    @Test
    @DisplayName("generate() - should start with ORD-")
    void generate_hasPrefix() {
        assertThat(OrderNumberGenerator.generate()).startsWith("ORD-");
    }

    @RepeatedTest(20)
    @DisplayName("generate() - should produce unique values across calls")
    void generate_uniqueAcrossCalls() {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            generated.add(OrderNumberGenerator.generate());
        }
        assertThat(generated).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("generate() - format matches ORD-YYYYMMDD-NNNNN")
    void generate_matchesFormat() {
        String num = OrderNumberGenerator.generate();
        assertThat(num).matches("ORD-\\d{8}-\\d{5}");
    }
}
