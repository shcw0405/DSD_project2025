package com.example.patientmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 统一API响应格式
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private T data;
    private String message;
    private Long total;

    public ApiResponse(int status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.total = null;
    }

    public ApiResponse(int status, T data, String message, Long total) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.total = total;
    }

    /**
     * 创建成功响应 (列表数据与总数)
     * @param data 列表数据
     * @param total 总数
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> ApiResponse<List<T>> success(List<T> data, long total, String message) {
        return new ApiResponse<>(200, data, message, total);
    }

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
        if (data instanceof DoctorListResponseDTO || data instanceof PatientListResponseDTO || data instanceof RelationListResponseDTO) {
            // 这是一个潜在的问题点，理想情况下应该通过编译时检查或更严格的类型来避免
            // 但为了不破坏现有代码，这里暂时不抛出异常，依赖开发者正确调用
            // System.out.println("警告: 为分页数据调用了不带 total 的 success 方法。请考虑使用 success(List<T> data, long total, String message)");
        }
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
        return new ApiResponse<>(500, null, message, null);
    }
}
