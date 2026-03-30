package com.myfinal.redis.exception;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.myfinal.redis.vo.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getCode())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(RestClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();
        String message = resolveMessage(exception);
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.fail(statusCode, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(500, exception.getMessage()));
    }

    private String resolveMessage(RestClientResponseException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return exception.getStatusText();
        }
        try {
            JSONObject jsonObject = JSON.parseObject(responseBody);
            String message = jsonObject.getString("message");
            if (message != null && !message.isBlank()) {
                return message;
            }
            String errorDescription = jsonObject.getString("error_description");
            if (errorDescription != null && !errorDescription.isBlank()) {
                return errorDescription;
            }
            String msg = jsonObject.getString("msg");
            if (msg != null && !msg.isBlank()) {
                return msg;
            }
        } catch (Exception ignored) {
        }
        return responseBody;
    }
}
