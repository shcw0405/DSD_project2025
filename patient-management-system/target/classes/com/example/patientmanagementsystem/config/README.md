# Configuration (配置层)

该目录包含应用的配置类，主要负责初始化、修复应用程序的关键设置，特别是管理员账户，以及配置如 OpenAPI 文档生成等服务。

## 主要组件与功能：

### 管理员账户初始化与修复：

系统包含多个在应用程序启动时运行的组件 (`CommandLineRunner` 或 `ApplicationRunner`)，用于确保管理员账户的正确设置。这些组件通过不同的策略（JPA、JDBC、组合方式）来创建、验证或修复管理员账户信息，包括用户名、密码、角色等。管理员的默认凭据通常从应用的配置文件 (`application.properties` 或 `application.yml`) 中读取。

- **`AdminAccountInitializer.java`**: (`ApplicationRunner`, `@Order(1)`)

  - 检查管理员账户是否存在。
  - 如果存在，确保角色为管理员并更新密码和名称。
  - 如果不存在，创建新的管理员账户。
  - 使用 JPA (`UserRepository`)。

- **`AdminUserInitializer.java`**: (`CommandLineRunner`)

  - 如果管理员账户不存在，则创建它，包含详细信息（姓名、电话、性别、证件等）和加密后的密码。
  - 如果存在，则记录信息，并可选地更新现有管理员的密码以匹配配置文件。
  - 使用 JPA (`UserRepository`)。

- **`AdminUserFixConfig.java`**: (`CommandLineRunner`)

  - **强制重新创建**管理员账户。
  - 首先删除可能已存在的同手机号管理员账户。
  - 然后创建一个全新的管理员账户，包含详细信息并确保密码正确加密。
  - 使用 JPA (`UserRepository`)。

- **`DirectAdminFixConfig.java`**: (`CommandLineRunner`)

  - 通过 **JDBC 直接操作数据库**来修复或创建管理员账户。
  - 检查账户是否存在，然后相应地执行 `UPDATE` 或 `INSERT` SQL 语句来设置管理员信息和角色。

- **`EmergencyAdminFixConfig.java`**: (`CommandLineRunner`)

  - 类似 `DirectAdminFixConfig`，通过 **JDBC 直接操作数据库**进行"紧急修复"。
  - 确保管理员账户存在、角色正确，并验证/更新密码。

- **`AdminRoleFixConfig.java`**: (`CommandLineRunner`)

  - 专注于修复管理员的**角色分配**。
  - 使用 JPA 查找用户，如果找到则校正其 `isAdmin`, `isDoctor`, `isPatient` 标志。
  - 如果未找到，则通过 JDBC 创建一个"紧急管理员账户"。

- **`AdminComprehensiveFixConfig.java`**: (`CommandLineRunner`)
  - 进行"全面修复"，结合 JPA 和 JDBC。
  - 确保管理员账户存在且角色正确（通过 JPA 和 JDBC 双重验证）。
  - 验证密码，并进一步**生成管理员的 JWT 令牌**并验证其中的角色，以确保安全机制正常工作。

**注意**: 多个管理员初始化/修复类作为 `CommandLineRunner` Bean 存在，它们会在应用启动时执行。它们的具体执行顺序（除了明确使用 `@Order` 的）以及它们之间的潜在交互和覆盖逻辑需要注意。

### API 文档配置：

- **`OpenApiConfig.java`**:
  - 配置 OpenAPI (Swagger v3) 文档生成。
  - 定义了 API 的基本信息（标题、描述、版本、联系方式、许可证）。
  - 配置了安全方案，指明 API 使用 JWT Bearer Token (`Authorization` 头部) 进行认证，这使得 Swagger UI 可以支持认证测试。

## 使用场景：

- **管理员账户管理**：这些配置确保了即使在数据库初始状态为空或管理员信息损坏的情况下，系统启动后总能有一个可用的、具有正确权限的管理员账户。
- **开发与部署**：在开发和部署初期，这些初始化器能够自动设置好必要的管理员账户，简化了初始设置步骤。
- **问题排查**："Fix"相关的配置可能是在遇到特定部署问题或数据不一致问题时引入的，作为一种自动化的纠错机制。
- **API 文档化**：`OpenApiConfig` 使得开发者和 API 消费者能够方便地浏览和测试 API 接口。
