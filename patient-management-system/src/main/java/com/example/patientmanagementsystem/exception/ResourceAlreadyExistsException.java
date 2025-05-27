package com.example.patientmanagementsystem.exception;

/**
 * 资源已存在异常
 * 当尝试创建已存在的资源时抛出此异常
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
    
    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s 已存在，%s: %s", resourceName, fieldName, fieldValue));
    }
}
