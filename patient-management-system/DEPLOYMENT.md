# 后端项目实操部署教程

本教程适用于本患者管理系统 Spring Boot 后端项目，适合新手快速上手。

---

## 1. 环境准备

- **JDK 版本**：建议 JDK 17 或更高
- **Maven**：建议 3.6 及以上
- **数据库**：支持 H2（内存）、MySQL、PostgreSQL 等，具体见 `application.properties`
- **Python**（可选）：如需使用 IMU 数据分析脚本，需安装 Python 3.x

## 2. 克隆代码

```bash
git clone xx.git/解压到本地
cd patient-management-system-complete/patient-management-system
```

## 3. 配置数据库

- 修改 `src/main/resources/application.properties`，配置数据库连接信息（如 MySQL，H2 等）。
- 配置管理员账号信息（如手机号、密码等）。

## 4. 安装依赖

```bash
mvn clean install
```

## 5. 运行后端服务

```bash
mvn spring-boot:run
```

或

```bash
java -jar target/patient-management-system-*.jar
```

## 6. 管理员账号初始化

- 系统启动时会自动初始化管理员账号（见 `config` 相关说明），如有需要可在配置文件中修改默认账号信息。

## 7. 数据分析脚本

- `src/main/resources/scripts/processorOfRawData.py` 为 IMU 数据处理脚本，需 Python 环境支持。
- 可根据实际需求在后端调用或手动运行。

## 8. 常见问题

- **端口冲突**：修改 `application.properties` 中的 `server.port`
- **数据库连接失败**：检查数据库配置、驱动依赖和网络连通性
- **管理员无法登录**：确认配置文件中的手机号和密码与数据库一致
