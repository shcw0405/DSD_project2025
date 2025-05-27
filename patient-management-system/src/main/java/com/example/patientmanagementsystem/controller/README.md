# Controller 层

该目录包含处理 HTTP 请求的控制器类，它们是应用的入口点，负责接收用户输入、调用相应的服务层逻辑，并返回 HTTP 响应。

## 主要功能：

- **用户认证与授权**：
  - `AuthController.java`: 处理用户登录认证请求。
  - `UserController.java`: 处理用户注册请求。
- **管理员功能**：
  - `AdminController.java`: 提供管理员对医生信息的增删改查接口，以及医生搜索功能。
  - `PatientController.java` (部分接口): 提供管理员对患者信息的增删改查接口，以及患者搜索功能。
  - `RelationController.java`: 提供管理员对医患关系的增删改查接口。
  - `AdminUserController.java`: (主要用于测试) 提供修改用户角色的接口。
- **医生功能**：
  - `DoctorController.java`: 提供医生获取其关联患者列表的接口。
  - `ReportController.java` (部分接口): 医生可以查看、上传、更新和下载患者报告。
  - `UploadController.java` (部分接口): 医生可以上传患者的 IMU 数据 CSV 文件进行分析。
- **患者功能**：
  - `PatientController.java` (部分接口): 授权用户（例如患者本人或其关联医生）可以获取患者的步态评估报告列表。
- **报告管理**：
  - `ReportController.java`: 处理患者报告的获取、详情查看、信息更新、上传和下载。
- **数据上传与处理**：
  - `UploadController.java`: 处理 IMU 数据的 CSV 文件上传，并进行后续的数据处理和分析，同时提供数据处理记录的查询。

## 通用特性：

- 大部分控制器接口都使用了 Spring Security 进行权限控制 (`@PreAuthorize`)。
- 统一使用 `ApiResponse` 对象来包装返回给客户端的数据，确保响应格式的一致性。
- 接口路径设计遵循 RESTful 风格。
- 使用 `@CrossOrigin` 注解处理跨域请求。
