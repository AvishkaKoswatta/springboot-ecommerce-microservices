package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String msg) { super(msg); }
    public PaymentNotFoundException(Long id) { super("Payment not found with id: " + id); }
}
