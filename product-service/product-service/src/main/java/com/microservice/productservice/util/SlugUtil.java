package com.microservice.productservice.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private SlugUtil() {}

    private static final Pattern NON_LATIN   = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE  = Pattern.compile("[\\s]");
    private static final Pattern MULTI_DASH  = Pattern.compile("-{2,}");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized
                .toLowerCase(Locale.ROOT);
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = MULTI_DASH.matcher(slug).replaceAll("-");
        return slug.replaceAll("^-|-$", "");
    }

    /**
     * Appends a numeric suffix to make the slug unique.
     * The uniqueness check is delegated to the caller.
     */
    public static String toUniqueSlug(String base, int attempt) {
        return attempt == 0 ? toSlug(base) : toSlug(base) + "-" + attempt;
    }
}
