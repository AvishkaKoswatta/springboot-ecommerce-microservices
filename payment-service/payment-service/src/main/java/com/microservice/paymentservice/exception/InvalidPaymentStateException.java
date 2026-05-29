package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidPaymentStateException extends RuntimeException {
    public InvalidPaymentStateException(String msg) { super(msg); }
}
