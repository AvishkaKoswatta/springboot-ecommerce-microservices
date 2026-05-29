package com.microservice.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RefundAlreadyExistsException extends RuntimeException {
    public RefundAlreadyExistsException(String msg) { super(msg); }
}
