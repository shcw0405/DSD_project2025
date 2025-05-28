# Service 层测试

该目录包含对服务层（Service）的单元测试代码，主要用于验证核心业务逻辑的正确性。

## 主要测试文件：

- `DoctorServiceTest.java`：对医生相关业务逻辑进行单元测试，包括医生的增删改查、搜索、关联患者等。
- `AuthServiceTest.java`：对认证服务（如登录、注册、权限校验等）进行单元测试。

这些测试通过 Mock 依赖（如 Repository、Encoder 等），确保服务层逻辑在不同输入和边界条件下的正确性。
