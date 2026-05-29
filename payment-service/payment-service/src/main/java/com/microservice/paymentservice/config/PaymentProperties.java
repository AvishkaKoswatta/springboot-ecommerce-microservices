package com.microservice.paymentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.payment")
@Getter @Setter
public class PaymentProperties {
    private int paymentExpiryMinutes = 15;
    private int refundWindowDays = 30;
}
