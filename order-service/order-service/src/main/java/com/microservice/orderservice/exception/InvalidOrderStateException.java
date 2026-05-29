package com.microservice.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String msg) { super(msg); }
}
