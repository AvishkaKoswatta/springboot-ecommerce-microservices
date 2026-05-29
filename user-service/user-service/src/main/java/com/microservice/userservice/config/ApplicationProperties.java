package com.microservice.userservice.config;
//Java representation of application.yml file
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationProperties {

    private Security security = new Security();
    private String frontendUrl;
    private Email email = new Email();

    @Getter
    @Setter
    public static class Security {
        private Jwt jwt = new Jwt();

        @Getter
        @Setter
        public static class Jwt {
            private String secretKey;
            private long expiration;
            private String tokenPrefix;
            private String headerString;
        }
    }

    @Getter
    @Setter
    public static class Email {
        private int verificationExpiryHours = 24;
        private int passwordResetExpiryMinutes = 30;
    }
}
