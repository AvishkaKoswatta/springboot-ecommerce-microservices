package com.microservice.paymentservice.entity;

public enum PaymentStatus {
    INITIATED,    // payment session created
    PROCESSING,   // mock gateway is processing
    SUCCESS,      // gateway confirmed payment
    FAILED,       // gateway rejected
    EXPIRED,      // session timed out
    CANCELLED,    // user cancelled
    REFUNDED,     // fully refunded
    PARTIALLY_REFUNDED
}
