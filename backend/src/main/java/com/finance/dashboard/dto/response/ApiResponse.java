package com.finance.dashboard.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final Map<String,String> errors;
    @Builder.Default private final LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> ok(T data) { return ApiResponse.<T>builder().success(true).data(data).build(); }
    public static <T> ApiResponse<T> ok(String msg, T data) { return ApiResponse.<T>builder().success(true).message(msg).data(data).build(); }
    public static <T> ApiResponse<T> error(String msg) { return ApiResponse.<T>builder().success(false).message(msg).build(); }
    public static <T> ApiResponse<T> validationError(String msg, Map<String,String> errors) { return ApiResponse.<T>builder().success(false).message(msg).errors(errors).build(); }
}
