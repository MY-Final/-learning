package com.myfinal.redis.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApiResponse<T> {

    private Integer code;

    private Boolean success;

    private String message;

    private T data;

    private LocalDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, Boolean success, String message, T data, LocalDateTime timestamp) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(200, true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<T>(code, false, message, null, LocalDateTime.now());
    }
}
