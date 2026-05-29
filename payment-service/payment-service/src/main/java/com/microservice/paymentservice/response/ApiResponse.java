package com.microservice.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer status;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).status(200).build();
    }
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder().success(true).message(message).status(200).build();
    }
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).status(201).build();
    }
    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder().success(false).message(message).status(code).build();
    }
}
