# Exception Handling

该目录包含自定义异常类和全局异常处理机制，用于统一管理和响应应用程序中发生的各种错误情况。

## 组件：

- **`GlobalExceptionHandler.java`**:

  - 使用 `@RestControllerAdvice` 注解，这是一个全局异常处理器。
  - 它捕获在控制器层或服务层抛出的各种异常，并将它们转换为标准化的 `ApiResponse` 对象返回给客户端。
  - 处理的异常类型包括：
    - `MethodArgumentNotValidException`: 请求参数验证失败。
    - `MethodArgumentTypeMismatchException`: 请求参数类型不匹配。
    - `MissingServletRequestParameterException`: 缺少必要的请求参数。
    - `AuthenticationException` (包括 `BadCredentialsException`): 用户认证失败（如用户名或密码错误）。
    - `AccessDeniedException`: 用户授权失败（权限不足）。
    - `EntityNotFoundException`: 请求的资源（如数据库实体）未找到。
    - `ResourceAlreadyExistsException`: 尝试创建已存在的资源。
    - `BusinessException`: 自定义的业务逻辑错误。
    - `CsvValidationException`: CSV 文件解析或验证错误。
    - `Exception`: 捕获所有其他未处理的通用异常，防止敏感信息泄露，并返回统一的服务器内部错误信息。
  - 通过统一处理异常，确保了 API 响应的一致性，并提供了更友好的错误提示给客户端。

- **自定义异常类**:
  - **`BusinessException.java`**:
    - 通用的业务逻辑异常。允许在抛出时指定错误消息和可选的 HTTP 状态码（默认为 400 Bad Request）。
  - **`CsvValidationException.java`**:
    - 专门用于 CSV 文件处理过程中的验证错误。
  - **`ResourceAlreadyExistsException.java`**:
    - 当尝试创建的资源（如用户、医生等）已经存在时抛出此异常。通常会导致 HTTP 409 Conflict 状态码。

## 工作流程：

1. 当应用程序的任何部分（通常是服务层）遇到错误条件时，会抛出相应的自定义异常或标准 Java/Spring 异常。
2. `GlobalExceptionHandler` 捕获这些抛出的异常。
3. 根据异常的类型，对应的 `@ExceptionHandler` 方法被调用。
4. 该方法记录错误日志（通常包含堆栈跟踪等详细信息，供开发人员调试），并构造一个包含用户友好错误消息和适当 HTTP 状态码的 `ApiResponse` 对象。
5. 这个 `ApiResponse` 对象被序列化为 JSON 并作为 HTTP 响应发送给客户端。

这种集中的异常处理策略有助于保持控制器代码的整洁，避免在每个控制器方法中重复编写异常处理逻辑，并确保了 API 错误响应的统一性和可预测性。
