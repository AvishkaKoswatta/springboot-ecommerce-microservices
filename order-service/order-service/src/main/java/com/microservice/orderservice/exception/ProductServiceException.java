package com.microservice.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ProductServiceException extends RuntimeException {
    public ProductServiceException(String msg) { super(msg); }
}
