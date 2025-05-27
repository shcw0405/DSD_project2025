# 主代码目录

该目录包含系统后端的所有核心代码模块，结构如下：

- `controller/`：控制器层，处理 HTTP 请求，负责路由分发和权限校验。
- `service/`：服务层，封装业务逻辑。
- `repository/`：数据访问层，负责与数据库的交互。
- `model/`：实体模型，定义数据库表结构和领域对象。
- `dto/`：数据传输对象，用于接口数据的输入输出。
- `mapper/`：对象转换器，实现实体与 DTO 之间的转换。
- `exception/`：异常处理，包含自定义异常和全局异常处理器。
- `config/`：配置类，包含系统初始化、管理员修复、OpenAPI 配置等。
- `security/`：安全相关配置和工具类。
- `util/`：通用工具类。
- `validation/`：自定义校验注解及其实现。
- `analysis/`：数据分析相关代码。

主启动类为 `PatientManagementSystemApplication.java`。

每个子目录下均有对应的 README.md 介绍具体内容。
