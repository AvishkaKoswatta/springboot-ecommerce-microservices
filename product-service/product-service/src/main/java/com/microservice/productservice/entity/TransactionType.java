package com.microservice.productservice.entity;

public enum TransactionType {
    STOCK_IN,          // Stock received from supplier
    STOCK_OUT,         // Stock sold / dispatched
    ADJUSTMENT,        // Manual admin adjustment
    RESERVATION,       // Stock reserved (e.g., in cart)
    RESERVATION_CANCEL,// Reservation released
    RETURN,            // Customer return
    DAMAGED,           // Written off as damaged
    INITIAL_STOCK      // First-time stock entry
}
