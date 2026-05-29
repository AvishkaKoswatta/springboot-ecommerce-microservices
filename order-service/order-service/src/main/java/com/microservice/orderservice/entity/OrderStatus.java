package com.microservice.orderservice.entity;

public enum OrderStatus {
    PENDING,        // Just placed, awaiting payment confirmation
    CONFIRMED,      // Payment received (or COD confirmed), being prepared
    PROCESSING,     // Being packed / fulfilled
    SHIPPED,        // Dispatched, tracking number assigned
    DELIVERED,      // Customer received
    CANCELLED,      // Cancelled before shipping
    REFUND_REQUESTED,
    REFUNDED,
    COMPLETED       // Delivered + return window closed
}
