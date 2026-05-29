package com.microservice.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(String msg) { super(msg); }
}
