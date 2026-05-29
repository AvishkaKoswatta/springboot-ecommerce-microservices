package com.microservice.paymentservice.entity;

public enum WebhookEventType {
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_EXPIRED,
    REFUND_PROCESSED,
    REFUND_FAILED
}
