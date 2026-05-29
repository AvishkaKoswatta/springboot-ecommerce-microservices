package com.microservice.orderservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "application.order")
@Getter @Setter
public class OrderProperties {
    private BigDecimal freeShippingThreshold = new BigDecimal("100.00");
    private BigDecimal flatShippingFee = new BigDecimal("5.99");
    private int cancellationWindowMinutes = 30;
    private int paymentTimeoutMinutes = 15;
}
