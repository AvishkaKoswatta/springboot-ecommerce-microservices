package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(String msg) { super(msg); }
}
