package com.example.patientmanagementsystem.exception;

import lombok.Getter;

/**
 * 业务逻辑异常
 * 用于表示业务逻辑错误，可以指定HTTP状态码
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int status;
    
    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }
    
    public BusinessException(String message) {
        this(message, 400);
    }

    public BusinessException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    
    public BusinessException(String message, Throwable cause) {
        this(message, 400, cause); // Default to 400 if status not provided
    }
}
