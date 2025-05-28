# Mapper 层

该目录包含 Mapper 类，这些类负责在不同的对象模型之间进行转换，特别是在领域实体（如 JPA Entities）和数据传输对象（DTOs）之间。

## 主要组件与功能：

- **`PatientMapper.java`**:

  - 这是一个 `@Component`，表明它是一个由 Spring 管理的 Bean，可以被注入到其他服务或组件中。
  - 主要功能是将 `Patient` 领域实体对象转换为 `PatientDTO` 数据传输对象，以及将分页的 `Patient` 实体列表 (`Page<Patient>`) 转换为包含 `PatientDTO` 列表和总记录数的 `PatientListResponseDTO`。

  - **`toDTO(Patient patient)` 方法**:

    - 接受一个 `Patient` 实体。
    - 将 `Patient` 实体的属性（如 `id`, `gender`, `birthDate`, `idNumber`）映射到 `PatientDTO` 的相应属性。
    - 从关联的 `User` 实体中获取患者的姓名 (`name`) 和电话 (`phone`) 并设置到 `PatientDTO` 中。
    - 处理 `Gender` 枚举到其字符串表示的转换。

  - **`toListResponseDTO(Page<Patient> patientPage)` 方法**:
    - 接受一个 `Page<Patient>` 对象（通常是 Spring Data JPA 分页查询的结果）。
    - 遍历分页结果中的 `Patient` 实体，使用 `toDTO` 方法将每个实体转换为 `PatientDTO`。
    - 将转换后的 `PatientDTO` 列表和分页信息中的总记录数封装到 `PatientListResponseDTO` 对象中，该对象通常用于 API 响应，以支持客户端的分页显示。

## 作用与优势：

- **解耦**：Mapper 将领域模型与 API 层或表示层使用的数据结构（DTOs）解耦。这意味着如果 DTO 的结构需要改变（例如，为了适应 API 的特定需求），领域模型不需要随之改变，反之亦然。
- **数据转换**：它们封装了数据转换的逻辑，使得代码更加清晰和模块化。例如，枚举到字符串的转换、从关联实体提取数据等。
- **代码复用**：转换逻辑被集中在一个地方，可以在多个服务或控制器中复用。
- **可测试性**：Mapper 类通常是无状态的，易于进行单元测试。

在这个项目中，`PatientMapper` 确保了患者信息在持久化层（实体）和 API 层（DTO）之间能够被正确和一致地转换。
