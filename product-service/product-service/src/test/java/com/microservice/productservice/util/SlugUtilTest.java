package com.microservice.productservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilTest {

    @ParameterizedTest(name = "''{0}'' → ''{1}''")
    @CsvSource({
            "Hello World,             hello-world",
            "iPhone 15 Pro Max,       iphone-15-pro-max",
            "  Leading/Trailing  ,    leadingtrailing",
            "Multiple   Spaces,       multiple-spaces",
            "Special@#$Characters!,   specialcharacters",
            "UPPERCASE TEXT,          uppercase-text",
    })
    @DisplayName("toSlug() - should correctly slugify various inputs")
    void toSlug_variousInputs(String input, String expected) {
        assertThat(SlugUtil.toSlug(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("toSlug() - should return empty string for null")
    void toSlug_null_returnsEmpty() {
        assertThat(SlugUtil.toSlug(null)).isEmpty();
    }

    @Test
    @DisplayName("toUniqueSlug() - should append suffix on attempt > 0")
    void toUniqueSlug_withAttempt() {
        assertThat(SlugUtil.toUniqueSlug("Hello World", 0)).isEqualTo("hello-world");
        assertThat(SlugUtil.toUniqueSlug("Hello World", 1)).isEqualTo("hello-world-1");
        assertThat(SlugUtil.toUniqueSlug("Hello World", 3)).isEqualTo("hello-world-3");
    }
}
