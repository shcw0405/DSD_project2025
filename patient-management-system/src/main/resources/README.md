# Application Resources (`src/main/resources`)

This directory contains all the externalized configuration and other resource files for the Patient Management System application.

## Overview

Spring Boot applications typically use this directory for a variety of purposes, including application properties, static assets (if any), and templates (if any). The contents help in managing application behavior without hardcoding values directly into the Java source code.

## Key Files and Directories

- **`application.properties`**

  - This is the primary configuration file for the Spring Boot application.
  - It contains settings such as:
    - Server configuration (e.g., `server.port`, `server.servlet.context-path`).
    - Database connection details (e.g., `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` for MySQL; H2 in-memory database settings for testing/development).
    - JPA/Hibernate properties (e.g., `spring.jpa.hibernate.ddl-auto`, `spring.jpa.show-sql`).
    - JWT (JSON Web Token) secret key and expiration time (`jwt.secret`, `jwt.expiration`).
    - File upload limitations and storage paths (`spring.servlet.multipart.max-file-size`, `spring.servlet.multipart.max-request-size`, `app.file.upload-dir`).
    - Logging levels and patterns (e.g., `logging.level.*`, `logging.pattern.console`, `logging.file.name`).
    - Default administrator account credentials (`app.admin.username`, `app.admin.password`).
    - OpenAPI/Springdoc configuration (e.g., `springdoc.api-docs.path`, `springdoc.swagger-ui.path`).

- **`static/`** (Directory)

  - This directory is conventionally used to serve static content such as HTML, CSS, JavaScript files, and images directly by Spring Boot's embedded web server.
  - _Currently, this project might not extensively use this directory, or it might be empty if the frontend is served separately or if it's a pure backend API._

- **`templates/`** (Directory)

  - If the application were to use server-side view templating engines like Thymeleaf, FreeMarker, or Velocity, template files would be placed here.
  - _Currently, this project primarily functions as a RESTful API backend and might not use server-side templates._

- **`data/`** (Directory - if present)

  - This directory is sometimes used to store sample data, CSV files for import, or SQL scripts for database initialization (though Spring Boot also has specific conventions for `data.sql` and `schema.sql`).
  - _The project might have used CSV files (like `patients.csv`, `doctors.csv`) for initial data setup, which could potentially reside here or at the project root._

- **`logs/`** (Directory - typically generated, not committed)
  - While the actual log files (e.g., `app.log` as configured in `application.properties`) are usually generated in a directory specified by `logging.file.name` (which could be at the project root or within `build`/`target`), this README notes that logging configuration is present in `application.properties`.
  - Log files themselves are typically excluded from version control via `.gitignore`.

---

Proper management of resources in this directory is crucial for application configurability, maintainability, and deployment across different environments.

# 资源目录

该目录用于存放 Spring Boot 项目的资源文件，包括：

- `application.properties`：Spring Boot 应用的主配置文件，包含数据库、端口、日志、管理员账号等配置信息。
- `scripts/`：存放与数据处理相关的脚本文件（如 Python 脚本）。
- 其他资源文件（如静态文件、模板等）可根据需要添加。
