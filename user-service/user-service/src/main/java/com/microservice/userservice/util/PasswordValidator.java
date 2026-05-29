package com.microservice.userservice.util;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private PasswordValidator() {}

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public static boolean isValid(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String getRequirements() {
        return "Password must be at least 8 characters and contain at least one uppercase letter, " +
               "one lowercase letter, one digit, and one special character (@$!%*?&)";
    }
}
