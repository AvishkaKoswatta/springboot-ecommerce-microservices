package com.microservice.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class GatewayException extends RuntimeException {
    public GatewayException(String msg) { super(msg); }
}
