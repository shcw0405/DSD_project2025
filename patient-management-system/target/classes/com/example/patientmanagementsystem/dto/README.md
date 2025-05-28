# DTO (Data Transfer Objects) 层

该目录包含数据传输对象 (DTO)。DTO 是简单的 Java 对象，用于在应用程序的不同层之间（例如，控制器和服务层，或服务层与外部系统）传递数据。它们有助于封装数据，定义清晰的数据结构，并减少方法参数的数量。

## 主要功能与用途：

- **请求封装**：将 HTTP 请求中的参数（如 JSON body、查询参数）封装成对象，方便在控制器和业务逻辑层处理。

  - 例如：`LoginDTO.java`, `RegisterDTO.java`, `DoctorRegistrationRequestDTO.java`, `AddRelationRequestDTO.java`, `UpdatePatientRequestDTO.java`, `UpdateDoctorRequestDTO.java`, `UpdateReportRequestDTO.java`。

- **响应封装**：将业务逻辑层处理的结果封装成特定的数据结构，以便控制器将其转换为 HTTP 响应返回给客户端。

  - 例如：`LoginResponseDTO.java`, `RegistrationSuccessDataDTO.java`, `DoctorRegistrationResponseDataDTO.java`, `PatientListResponseDTO.java`, `DoctorListResponseDTO.java`, `RelationListResponseDTO.java`, `PatientReportDetailDTO.java`, `UpdateReportResponseDataDTO.java`, `UploadResponseDataDTO.java`, `ApiResponse.java` (通用响应结构)。

- **数据表示**：定义用于在不同组件间传递的核心实体或聚合数据的结构。
  - 例如：`UserDTO.java`, `DoctorDTO.java`, `PatientDTO.java`, `PatientReportDTO.java`, `ReportDataDTO.java`, `ReportDataDetailsDTO.java`, `ReportDownloadDTO.java`, `DoctorPatientRelationDTO.java`。

## 命名约定：

- 通常以 `DTO` 后缀结尾。
- 根据其用途命名，例如 `RequestDTO` 表示请求数据，`ResponseDTO` 表示响应数据。

DTO 的使用有助于保持代码的模块化和可维护性，使得数据交换更加规范和高效。
