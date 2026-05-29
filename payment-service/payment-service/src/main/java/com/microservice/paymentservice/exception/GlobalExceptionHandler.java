package com.microservice.paymentservice.exception;

import com.microservice.paymentservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyExists(PaymentAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidPaymentStateException ex) {
        return ResponseEntity.status(409).body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(RefundNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefundNotAllowed(RefundNotAllowedException ex) {
        return ResponseEntity.status(422).body(ApiResponse.error(ex.getMessage(), 422));
    }

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ApiResponse<Void>> handleGateway(GatewayException ex) {
        log.error("Gateway error: {}", ex.getMessage());
        return ResponseEntity.status(502).body(ApiResponse.error(ex.getMessage(), 502));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(ApiResponse.error("Access denied", 403));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            String field = ((FieldError) e).getField();
            errors.put(field, e.getDefaultMessage());
        });
        return ResponseEntity.status(400)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false).message("Validation failed").data(errors).status(400).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage(), 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(ApiResponse.error("An unexpected error occurred", 500));
    }
}
