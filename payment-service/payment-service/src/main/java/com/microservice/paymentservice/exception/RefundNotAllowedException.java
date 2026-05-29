package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class RefundNotAllowedException extends RuntimeException {
    public RefundNotAllowedException(String msg) { super(msg); }
}
