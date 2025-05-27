package com.example.patientmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private T data;
    private String message;

    /**
     * 创建成功响应
     * @param data 响应数据
     * @param message 成功消息
     * @param status 状态码
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(status, data, message);
    }

    /**
     * 创建成功响应（状态码200）
     * @param data 响应数据
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, data, message);
    }

    /**
     * 创建成功响应（状态码200，默认成功消息）
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, "操作成功");
    }

    /**
     * 创建成功响应（无数据，状态码200）
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, null, message);
    }

    /**
     * 创建创建成功响应（状态码201）
     * @param data 响应数据
     * @param message 成功消息
     * @return 创建成功响应
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(201, data, message);
    }

    /**
     * 创建删除成功响应（状态码204）
     * @param message 成功消息
     * @return 删除成功响应
     */
    public static <T> ApiResponse<T> deleted(String message) {
        return new ApiResponse<>(204, null, message);
    }

    /**
     * 创建错误响应
     * @param status 错误状态码
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, null, message);
    }

    /**
     * 创建参数错误响应（状态码400）
     * @param message 错误消息
     * @return 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, null, message);
    }

    /**
     * 创建未授权响应（状态码401）
     * @param message 错误消息
     * @return 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, null, message);
    }

    /**
     * 创建权限不足响应（状态码403）
     * @param message 错误消息
     * @return 权限不足响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, null, message);
    }

    /**
     * 创建资源不存在响应（状态码404）
     * @param message 错误消息
     * @return 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, null, message);
    }

    /**
     * 创建冲突响应（状态码409）
     * @param message 错误消息
     * @return 冲突响应
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, null, message);
    }

    /**
     * 创建服务器错误响应（状态码500）
     * @param message 错误消息
     * @return 服务器错误响应
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, null, message);
    }
}
