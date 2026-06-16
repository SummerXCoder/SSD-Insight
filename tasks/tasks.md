# tasks.md — 企业级会议室预约系统 任务清单

> **状态：** 待评审
> **基于：** spec.md v1.0 / plan.md v1.0 / constitution.md v3.2 / AGENTS.md v3.2
> **日期：** 2026-06-12
> **技术栈：** Java 17+ / Spring Boot 3.x / MyBatis / Vue 3 + TypeScript + Vite

---

## 前置预检报告 (Pre-flight Check)

### 一致性检查

- `plan.md` 与 `spec.md` 核心业务规则一致：12 个场景、18 条验收标准、7 条业务不变量均已覆盖。
- `plan.md` 的 7 条假设（ASM-P01~P07）均为合理补全，未与 `spec.md` 冲突。
- API 路由骨架与 `spec.md` 场景一一对应，无遗漏。
- **无重大冲突或缺失**，可继续生成任务清单。

### 技术栈确认

- **后端：** Java 17+ / Spring Boot 3.x / MyBatis / MySQL 8.x / HikariCP / Spring Security (Session)
- **前端：** Vue 3 / TypeScript / Vite / Pinia / Element Plus（UI 组件库）
- **测试：** JUnit 5 + AssertJ + Mockito（后端）/ Vitest + Vue Test Utils（前端）
- **构建：** Maven（后端）/ npm/pnpm（前端）

### 工作目录确认

- **项目根目录：** `c:\WorkSpace\aiproject`
- **后端根目录：** `backend/`（待创建）
- **前端根目录：** `frontend/`（待创建）

### 前后端边界确认

- 后端：`backend/src/main/java/com/company/meeting/` — Domain / Application / Infrastructure / Interfaces 四层
- 前端：`frontend/src/` — views / components / composables / stores / services / models / router / utils
- 共享类型：前端 `models/` 与后端 `dto/` 通过代码审查保持一致，无自动生成

---

## Phase 1：基础骨架与公共约束

> 目标：搭建后端 Spring Boot 项目骨架、前端 Vue 3 项目骨架、公共异常处理、统一错误响应、数据库建表。

- [ ] T001 [ARCH-D1] [Phase 1] [Size: M] 初始化 Spring Boot 3.x 后端项目骨架，配置 Maven 依赖（Spring Boot Starter Web / MyBatis / MySQL / Spring Security / Validation / MapStruct / Lombok），建立 `com.company.meeting` 四层目录结构（domain / application / infrastructure / interfaces）。 依赖：无。 文件：`backend/pom.xml`、`backend/src/main/java/com/company/meeting/` 目录结构、`backend/src/main/resources/application.yml`、`backend/src/main/resources/application-dev.yml`。 验证：`mvn compile` 成功，目录结构符合 plan.md §2.3。 完成判据：项目可编译，四层包结构已创建，`application.yml` 含数据库连接占位配置。 来源：`plan.md#2.3 系统结构建议, §1.4 D1`

- [ ] T002 [ARCH-D5] [Phase 1] [Size: M] 初始化 Vue 3 + TypeScript + Vite 前端项目骨架，安装核心依赖（Vue Router / Pinia / Axios / Element Plus），建立 `src/` 目录结构（views / components / composables / stores / services / models / router / utils），配置 Vite 代理与环境变量。 依赖：无。 文件：`frontend/package.json`、`frontend/vite.config.ts`、`frontend/tsconfig.json`、`frontend/.env.development`、`frontend/.env.production`、`frontend/src/` 目录结构。 验证：`npm run dev` 启动成功，Vite 代理配置指向后端。 完成判据：前端项目可启动，目录结构符合 plan.md §2.3。 来源：`plan.md#2.3 前端划分, §1.4 D5`

- [ ] T003 [ARCH-D1] [Phase 1] [Size: S] 实现后端统一异常处理与错误响应格式。创建 `BusinessException`（映射 400）、`GlobalExceptionHandler`（@ControllerAdvice），统一错误响应体 `{code, message, timestamp}`。 依赖：T001。 文件：`backend/src/main/java/com/company/meeting/infrastructure/exception/BusinessException.java`、`backend/src/main/java/com/company/meeting/infrastructure/exception/GlobalExceptionHandler.java`、`backend/src/main/java/com/company/meeting/infrastructure/exception/ErrorResponse.java`。 验证：手动抛出 BusinessException，确认返回 400 + 正确 JSON 格式。 完成判据：异常响应格式符合 plan.md §3.2 规范。 来源：`plan.md#4.1 防御性策略, §3.2 异常响应规范`

- [ ] T004 [ARCH-D1] [Phase 1] [Size: S] 实现后端领域层 Result 模式。创建泛型 `Result<T>` 类，含 `isSuccess()` / `isFailure()` / `getCode()` / `getMessage()` / `getData()` 方法，以及静态工厂方法 `success()` / `failure()`。 依赖：T001。 文件：`backend/src/main/java/com/company/meeting/domain/types/Result.java`。 验证：单元测试验证 success/failure 构造与状态判断。 完成判据：Result 类可正确表达业务成功与失败，无框架依赖。 来源：`plan.md#4.1 Result vs Exception 边界, 附录 设计模式`

- [ ] T005 [ARCH-D1] [Phase 1] [Size: S] 定义后端业务错误码枚举 `BusinessErrorCode`，覆盖所有可预期的业务错误（RESERVATION_CONFLICT / TIME_CONSTRAINT_VIOLATION / RESERVATION_BANNED / ROOM_DISABLED / CHECK_IN_NOT_AVAILABLE / DUPLICATE_USERNAME / UNAUTHORIZED / FORBIDDEN / RESOURCE_NOT_FOUND）。 依赖：T003。 文件：`backend/src/main/java/com/company/meeting/infrastructure/exception/BusinessErrorCode.java`。 验证：枚举值覆盖 plan.md §3.2 与 §4.1 中所有业务错误场景。 完成判据：所有业务错误码已定义，每个码含 code 字符串和默认 message。 来源：`plan.md#3.2, §4.1`

- [ ] T006 [Phase 1] [Size: M] 设计并创建 MySQL 数据库表结构（DDL），包含 meeting_room / user / department / reservation / notification 五张表，含索引与唯一约束（reservation 表 room_id + 日期范围唯一约束作为冲突检测兜底）。 依赖：T001。 文件：`backend/src/main/resources/db/schema.sql`、`backend/src/main/resources/db/data-init.sql`（初始部门与管理员数据）。 验证：在 MySQL 中执行 schema.sql，表创建成功，唯一约束生效。 完成判据：DDL 符合 plan.md §2.1 领域模型，唯一约束覆盖 INV-01 兜底策略。 来源：`plan.md#2.1, §6.3 并发控制策略`

- [ ] T007 [ARCH-D5] [Phase 1] [Size: S] 实现前端统一 API Client（Axios 实例 + 请求/响应拦截器），处理鉴权（Session Cookie）、统一错误反馈（ElMessage Toast）、加载状态管理。 依赖：T002。 文件：`frontend/src/services/api.ts`。 验证：手动调用不存在的 API，确认 Toast 展示错误消息。 完成判据：拦截器正确处理 401/403/4xx/5xx，错误消息通过 ElMessage 展示。 来源：`plan.md#3.3 前后端联动验证约束, §1.4 D5`

---

## Phase 2：核心领域模型与领域测试

> 目标：实现 Domain 层核心模型（实体、值对象、枚举、领域服务），以表格驱动测试覆盖所有业务不变量。

### 2.1 值对象与枚举

- [ ] T008 [INV-05] [AC-09] [Phase 2] [Size: S] 编写 TimeSlot 值对象的单元测试，覆盖 plan.md §5.2 核心逻辑 2（时间约束校验）全部 5 个场景：提前量≥1h、时长≤2h、范围≤14天、边界值。 依赖：T004。 文件：`backend/src/test/java/com/company/meeting/domain/types/TimeSlotTest.java`。 验证：执行测试，5 个场景全部断言通过。 完成判据：测试覆盖 INV-05 全部约束规则，命名引用 AC-09。 来源：`spec.md#INV-05, AC-09`; `plan.md#5.2 核心逻辑 2`

- [ ] T009 [INV-05] [Phase 2] [Size: S] 实现 TimeSlot 值对象以通过 T008 测试。TimeSlot 含 startTime / endTime，自校验方法 `validateConstraints(LocalDateTime now)` 返回 `Result<Void>`。 依赖：T008。 文件：`backend/src/main/java/com/company/meeting/domain/types/TimeSlot.java`。 验证：执行 T008 测试全部通过。 完成判据：TimeSlot 自校验逻辑满足 INV-05，无框架依赖。 来源：`spec.md#INV-05`; `plan.md#2.1, §2.2`

- [ ] T010 [Phase 2] [Size: S] 实现领域枚举类型：ReservationStatus（PENDING / APPROVED / REJECTED / CANCELLED / CHECKED_IN / NO_SHOW）、RoomStatus（ENABLED / DISABLED）、UserRole（REGULAR / ADMIN）、Equipment（PROJECTOR / WHITEBOARD / VIDEO_CONF 等）。 依赖：T001。 文件：`backend/src/main/java/com/company/meeting/domain/types/ReservationStatus.java`、`RoomStatus.java`、`UserRole.java`、`Equipment.java`。 验证：编译通过，枚举值与 spec.md §2 术语表一致。 完成判据：所有枚举值严格映射 spec.md 领域词汇。 来源：`spec.md#2 术语表`; `plan.md#2.1`

- [ ] T011 [INV-01] [AC-02] [Phase 2] [Size: S] 编写冲突检测的单元测试，覆盖 plan.md §5.2 核心逻辑 1 全部 8 个场景（端点相邻不冲突、部分重叠、完全包含、完全相同、完全不重叠、无已有预约）。 依赖：T009。 文件：`backend/src/test/java/com/company/meeting/domain/service/ReservationDomainServiceTest.java`。 验证：执行测试，8 个冲突检测场景全部断言通过。 完成判据：测试覆盖 INV-01 全部边界条件，命名引用 AC-02。 来源：`spec.md#INV-01, AC-02`; `plan.md#5.2 核心逻辑 1`

### 2.2 领域实体

- [ ] T012 [Phase 2] [Size: S] 实现领域实体 MeetingRoom，含属性 id / name / location / capacity / equipmentList / status，含启用/停用状态切换方法。 依赖：T010。 文件：`backend/src/main/java/com/company/meeting/domain/model/MeetingRoom.java`。 验证：编译通过，属性与 spec.md §2 术语表一致。 完成判据：MeetingRoom 实体属性完整，状态切换方法内聚。 来源：`spec.md#2 术语表`; `plan.md#2.1`

- [ ] T013 [Phase 2] [Size: S] 实现领域实体 User，含属性 id / username / password / role / departmentId / noShowCount / reservationBan（嵌入值对象），含禁令判定方法 `isBanned()` / `addNoShow()` / `checkAndApplyBan()`。 依赖：T010。 文件：`backend/src/main/java/com/company/meeting/domain/model/User.java`、`backend/src/main/java/com/company/meeting/domain/model/ReservationBan.java`。 验证：编译通过，禁令判定逻辑与 spec.md SCN-12 一致。 完成判据：User 实体含未到场计数与禁令逻辑，方法内聚。 来源：`spec.md#SCN-12`; `plan.md#2.1`

- [ ] T014 [INV-07] [AC-17] [AC-18] [Phase 2] [Size: S] 编写未到场禁令判定的单元测试，覆盖 plan.md §5.2 核心逻辑 4 全部 4 个场景（累计3次→7天禁令、累计5次→30天禁令、未达阈值、已在禁令中）。 依赖：T013。 文件：`backend/src/test/java/com/company/meeting/domain/model/UserBanTest.java`。 验证：执行测试，4 个禁令判定场景全部断言通过。 完成判据：测试覆盖 INV-07 全部阶梯规则，命名引用 AC-17/AC-18。 来源：`spec.md#INV-07, AC-17, AC-18`; `plan.md#5.2 核心逻辑 4`

- [ ] T015 [Phase 2] [Size: S] 实现领域实体 Department，含属性 id / name / approverId。 依赖：T010。 文件：`backend/src/main/java/com/company/meeting/domain/model/Department.java`。 验证：编译通过。 完成判据：Department 实体属性完整。 来源：`spec.md#2 术语表`; `plan.md#2.1`

- [ ] T016 [Phase 2] [Size: S] 实现领域实体 Notification，含属性 id / userId / content / read / reservationId / createdAt，含标记已读方法。 依赖：T001。 文件：`backend/src/main/java/com/company/meeting/domain/model/Notification.java`。 验证：编译通过。 完成判据：Notification 实体属性完整，符合 ASM-P05 简单消息模型。 来源：`spec.md#2 术语表`; `plan.md#2.1, ASM-P05`

- [ ] T017 [Phase 2] [Size: M] 实现聚合根 Reservation，含属性 id / roomId / userId / timeSlot / status / meetingName / attendeeCount / createdAt / updatedAt，含状态流转方法（submit / approve / reject / cancel / checkIn / markNoShow），状态机校验内聚。 依赖：T009, T010。 文件：`backend/src/main/java/com/company/meeting/domain/model/Reservation.java`。 验证：编译通过，状态流转方法与 spec.md §5 状态机一致。 完成判据：Reservation 聚合根状态流转内聚，非法流转抛出显式异常。 来源：`spec.md#5 业务不变量`; `plan.md#2.2 状态流转`

### 2.3 领域服务

- [ ] T018 [INV-01] [INV-04] [INV-07] [AC-01] [AC-02] [AC-09] [AC-10] [AC-18] [Phase 2] [Size: M] 实现 ReservationDomainService 领域服务，包含：①冲突检测方法 `checkConflict()`（依赖 ReservationRepository 查询同室同日预约，排除自身）；②预约提交校验方法 `validateSubmission()`（校验会议室状态、时间约束、用户禁令、冲突检测）；③预约修改校验方法 `validateUpdate()`（同上，排除自身）。 依赖：T011, T012, T017。 文件：`backend/src/main/java/com/company/meeting/domain/service/ReservationDomainService.java`。 验证：执行 T011 冲突检测测试通过；补充 INV-04/INV-07 校验测试通过。 完成判据：领域服务覆盖 INV-01/INV-04/INV-05/INV-07，返回 Result 类型。 来源：`spec.md#INV-01, INV-04, INV-05, INV-07`; `plan.md#2.2, §2.4`

- [ ] T019 [INV-01] [AC-02] [Phase 2] [Size: S] 编写 ReservationDomainService 冲突检测的补充单元测试，验证与已有 PENDING 和 APPROVED 预约的冲突检测（INV-01 核心规则：待审批预约也占用时间段）。 依赖：T018。 文件：`backend/src/test/java/com/company/meeting/domain/service/ReservationDomainServiceConflictTest.java`。 验证：执行测试，PENDING 和 APPROVED 预约均被纳入冲突检测。 完成判据：测试显式验证 INV-01 "待审批预约也占用时间段" 规则。 来源：`spec.md#INV-01`; `plan.md#2.2`

- [ ] T020 [AC-14] [AC-15] [Phase 2] [Size: S] 编写签到窗口校验的单元测试，覆盖 plan.md §5.2 核心逻辑 3 全部 5 个场景（开始前15分钟、开始后15分钟、开始前>15分钟、开始后>15分钟、恰好开始）。 依赖：T017。 文件：`backend/src/test/java/com/company/meeting/domain/service/CheckInDomainServiceTest.java`。 验证：执行测试，5 个签到窗口场景全部断言通过。 完成判据：测试覆盖 AC-14/AC-15 全部边界条件。 来源：`spec.md#AC-14, AC-15`; `plan.md#5.2 核心逻辑 3`

- [ ] T021 [AC-14] [AC-15] [Phase 2] [Size: S] 实现 CheckInDomainService 领域服务以通过 T020 测试，含签到窗口校验方法 `isWithinCheckInWindow(LocalDateTime now, TimeSlot timeSlot)`。 依赖：T020。 文件：`backend/src/main/java/com/company/meeting/domain/service/CheckInDomainService.java`。 验证：执行 T020 测试全部通过。 完成判据：签到窗口校验逻辑满足 SCN-11 规则。 来源：`spec.md#SCN-11`; `plan.md#2.4`

### 2.4 Gateway 接口（仓储接口）

- [ ] T022 [ARCH-D1] [Phase 2] [Size: S] 定义 Domain 层 Gateway 接口：ReservationRepository / MeetingRoomRepository / UserRepository / DepartmentRepository / NotificationRepository，含核心查询方法签名（冲突检测查询、分页查询等）。 依赖：T017, T012, T013, T015, T016。 文件：`backend/src/main/java/com/company/meeting/domain/gateway/ReservationRepository.java`、`MeetingRoomRepository.java`、`UserRepository.java`、`DepartmentRepository.java`、`NotificationRepository.java`。 验证：编译通过，接口方法覆盖 plan.md §2.4 关键抽象。 完成判据：Gateway 接口定义完整，Domain 层无框架依赖。 来源：`plan.md#2.4 关键抽象, 附录 依赖倒置`

---

## Phase 3：应用层与后端主链路

> 目标：实现 Application Service、Controller、DTO/Assembler、认证、审批流程、定时任务，API 集成测试覆盖 AC-01~AC-18。

### 3.1 基础设施层 — 持久化

- [ ] T023 [Phase 3] [Size: M] 实现 Infrastructure 层 MyBatis 持久化：创建 PO 实体类（与数据库表映射）、Mapper 接口（含 XML 映射文件）、Repository 实现（实现 Gateway 接口），覆盖 Reservation / MeetingRoom / User / Department / Notification 五个聚合。 依赖：T006, T022。 文件：`backend/src/main/java/com/company/meeting/infrastructure/persistence/entity/` PO 类、`backend/src/main/java/com/company/meeting/infrastructure/persistence/mapper/` Mapper 接口、`backend/src/main/resources/mapper/` XML 映射、`backend/src/main/java/com/company/meeting/infrastructure/persistence/repository/` Repository 实现。 验证：Spring Boot 启动成功，Mapper 注入无报错；手动调用 Repository 方法可 CRUD。 完成判据：五个 Repository 实现均通过 Gateway 接口编译检查，MyBatis 映射正确。 来源：`plan.md#2.3 系统结构建议`

- [ ] T024 [Phase 3] [Size: S] 实现 ReservationRepository 的冲突检测查询方法，使用 `SELECT ... FOR UPDATE` 锁定同一会议室当日预约行，支持并发安全的冲突检测。 依赖：T023。 文件：`backend/src/main/java/com/company/meeting/infrastructure/persistence/mapper/ReservationMapper.java`（增加 `findByRoomIdAndDateRangeForUpdate` 方法）、`backend/src/main/resources/mapper/ReservationMapper.xml`。 验证：单元测试验证 FOR UPDATE 查询返回正确结果集。 完成判据：冲突检测查询使用排他锁，符合 plan.md §6.3 并发控制策略。 来源：`plan.md#6.3 并发控制策略`

### 3.2 认证

- [ ] T025 [AC-12] [Phase 3] [Size: S] 编写用户注册/登录 API 的契约/集成测试，覆盖 Happy Path（注册成功→201、登录成功→200+Session）与异常路径（用户名重复→400、凭证错误→401、未登录访问→401）。 依赖：T023。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/AuthControllerTest.java`。 验证：执行测试，成功路径与异常路径均被断言。 完成判据：测试覆盖 AC-12，状态码与响应体断言清晰。 来源：`spec.md#AC-12, SCN-08`; `plan.md#3.2 API 路由`

- [ ] T026 [AC-12] [Phase 3] [Size: M] 实现用户注册/登录/登出 API 逻辑（AuthController + UserApplicationService），集成 Spring Security Session 认证，密码使用 BCrypt 哈希（ASM-P01）。 依赖：T025, T005。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/AuthController.java`、`backend/src/main/java/com/company/meeting/application/service/UserApplicationService.java`、`backend/src/main/java/com/company/meeting/infrastructure/security/SecurityConfig.java`。 验证：执行 T025 测试全部通过。 完成判据：注册/登录/登出 API 满足 AC-12，Session 认证生效，BCrypt 哈希存储。 来源：`spec.md#SCN-08, AC-12`; `plan.md#1.4 D4, ASM-P01`

### 3.3 会议室管理 API

- [ ] T027 [Phase 3] [Size: S] 编写会议室 CRUD API 的契约/集成测试，覆盖：新增会议室→201、名称重复→400、修改会议室→200、删除有关联预约的会议室→400、删除无关联预约→200、列表查询（含筛选与分页）。 依赖：T023。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/MeetingRoomControllerTest.java`。 验证：执行测试，全部场景断言通过。 完成判据：测试覆盖会议室 CRUD 全部异常路径。 来源：`spec.md#SCN-06`; `plan.md#3.2 API 路由`

- [ ] T028 [Phase 3] [Size: M] 实现会议室 CRUD API 逻辑（MeetingRoomController + MeetingRoomApplicationService），含 DTO / Assembler（MapStruct），支持列表筛选（容量/设备/状态）与分页（默认20条，ASM-P06）。 依赖：T027, T023。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/MeetingRoomController.java`、`backend/src/main/java/com/company/meeting/application/service/MeetingRoomApplicationService.java`、`backend/src/main/java/com/company/meeting/application/dto/` DTO 类、`backend/src/main/java/com/company/meeting/interfaces/web/assembler/MeetingRoomAssembler.java`。 验证：执行 T027 测试全部通过。 完成判据：会议室 CRUD API 满足 SCN-06，分页与筛选符合 ASM-P06。 来源：`spec.md#SCN-06`; `plan.md#3.2, ASM-P06`

- [ ] T029 [AC-10] [AC-11] [AC-13] [Phase 3] [Size: S] 编写会议室停用/启用 API 的契约/集成测试，覆盖：停用→关联未来预约全部取消+通知→200、停用后预约→400+ROOM_DISABLED、启用→恢复可预约→200。 依赖：T028。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/MeetingRoomDisableEnableTest.java`。 验证：执行测试，AC-10/AC-11/AC-13 场景断言通过。 完成判据：测试覆盖 AC-10/AC-11/AC-13，停用时关联预约取消与通知创建被验证。 来源：`spec.md#AC-10, AC-11, AC-13, SCN-07`; `plan.md#3.2`

- [ ] T030 [AC-10] [AC-11] [AC-13] [Phase 3] [Size: M] 实现会议室停用/启用 API 逻辑，停用时在同一事务内：更新会议室状态 + 批量取消关联未来预约 + 批量创建通知。启用时恢复状态。 依赖：T029, T028。 文件：`backend/src/main/java/com/company/meeting/application/service/MeetingRoomApplicationService.java`（增加 disableRoom / enableRoom 方法）。 验证：执行 T029 测试全部通过。 完成判据：停用/启用逻辑满足 AC-10/AC-11/AC-13，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-07, AC-10, AC-11, AC-13`; `plan.md#6.4 事务边界`

- [ ] T031 [Phase 3] [Size: S] 实现查询会议室可用时间段 API（GET /api/rooms/{id}/availability?date=），返回指定日期的可用时间段列表。 依赖：T028。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/MeetingRoomController.java`（增加 availability 方法）、`backend/src/main/java/com/company/meeting/application/dto/AvailabilityResponse.java`。 验证：API 测试验证返回的可用时间段排除了已有预约。 完成判据：可用时间段查询正确排除 PENDING 和 APPROVED 预约。 来源：`plan.md#3.2 API 路由`

### 3.4 预约 API

- [ ] T032 [AC-01] [AC-02] [AC-09] [AC-10] [AC-18] [Phase 3] [Size: M] 编写预约提交 API 的契约/集成测试，覆盖：无冲突提交→201+PENDING+通知（AC-01）、冲突提交→400+RESERVATION_CONFLICT（AC-02）、时间约束违规→400+TIME_CONSTRAINT_VIOLATION（AC-09）、停用室预约→400+ROOM_DISABLED（AC-10）、禁令期间预约→400+RESERVATION_BANNED（AC-18）。 依赖：T026, T028。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/ReservationCreateTest.java`。 验证：执行测试，5 个 AC 场景全部断言通过。 完成判据：测试覆盖 AC-01/AC-02/AC-09/AC-10/AC-18，状态码与业务错误码断言清晰。 来源：`spec.md#AC-01, AC-02, AC-09, AC-10, AC-18`; `plan.md#3.2, §5.3`

- [ ] T033 [AC-01] [AC-02] [AC-09] [AC-10] [AC-18] [Phase 3] [Size: M] 实现预约提交 API 逻辑（ReservationController.createReservation + ReservationApplicationService.createReservation），在同一事务内：调用领域服务校验 → 创建预约（PENDING）→ 创建通知 → 返回。 依赖：T032, T018, T024。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/ReservationController.java`、`backend/src/main/java/com/company/meeting/application/service/ReservationApplicationService.java`、`backend/src/main/java/com/company/meeting/application/dto/` 预约相关 DTO、`backend/src/main/java/com/company/meeting/interfaces/web/assembler/ReservationAssembler.java`。 验证：执行 T032 测试全部通过。 完成判据：预约提交逻辑满足 AC-01/AC-02/AC-09/AC-10/AC-18，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-01`; `plan.md#6.4 事务边界`

- [ ] T034 [AC-05] [Phase 3] [Size: S] 编写取消预约 API 的契约/集成测试，覆盖：取消自己的预约→200+CANCELLED+时间段释放+审批人通知（AC-05）、已取消预约再取消→400、非本人取消→403。 依赖：T033。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/ReservationCancelTest.java`。 验证：执行测试，AC-05 与异常路径断言通过。 完成判据：测试覆盖 AC-05，时间段释放与通知创建被验证。 来源：`spec.md#AC-05, SCN-02`; `plan.md#3.2`

- [ ] T035 [AC-05] [Phase 3] [Size: S] 实现取消预约 API 逻辑，在同一事务内：校验预约人 → 更新状态为 CANCELLED → 释放时间段 → 创建通知。 依赖：T034。 文件：`backend/src/main/java/com/company/meeting/application/service/ReservationApplicationService.java`（增加 cancelReservation 方法）。 验证：执行 T034 测试全部通过。 完成判据：取消逻辑满足 AC-05，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-02, AC-05`; `plan.md#6.4`

- [ ] T036 [AC-06] [AC-07] [AC-08] [Phase 3] [Size: M] 编写修改预约 API 的契约/集成测试，覆盖：修改无冲突→200+PENDING+原时间段释放+新时间段占用（AC-06）、修改有冲突→400+RESERVATION_CONFLICT（AC-07）、被拒绝预约修改重提→200+PENDING（AC-08）、非本人修改→403。 依赖：T033。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/ReservationUpdateTest.java`。 验证：执行测试，AC-06/AC-07/AC-08 与异常路径断言通过。 完成判据：测试覆盖 AC-06/AC-07/AC-08，时间段释放/占用与状态重置被验证。 来源：`spec.md#AC-06, AC-07, AC-08, SCN-03, SCN-04`; `plan.md#3.2`

- [ ] T037 [AC-06] [AC-07] [AC-08] [Phase 3] [Size: M] 实现修改预约 API 逻辑，在同一事务内：校验预约人 → 释放原时间段 → 调用领域服务校验新时间段 → 更新预约（重置为 PENDING）→ 占用新时间段 → 创建通知。 依赖：T036。 文件：`backend/src/main/java/com/company/meeting/application/service/ReservationApplicationService.java`（增加 updateReservation 方法）。 验证：执行 T036 测试全部通过。 完成判据：修改逻辑满足 AC-06/AC-07/AC-08，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-03, SCN-04, AC-06, AC-07, AC-08`; `plan.md#6.4`

- [ ] T038 [Phase 3] [Size: S] 实现我的预约列表 API（GET /api/reservations/mine?status=&page=&size=）与预约详情 API（GET /api/reservations/{id}），含分页与状态筛选。 依赖：T033。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/ReservationController.java`（增加 listMine / getDetail 方法）。 验证：API 测试验证分页与筛选正确。 完成判据：列表 API 支持分页与状态筛选，详情 API 返回完整预约信息。 来源：`plan.md#3.2 API 路由`

### 3.5 审批 API

- [ ] T039 [AC-03] [AC-04] [Phase 3] [Size: S] 编写审批 API 的契约/集成测试，覆盖：审批通过→200+APPROVED+用户通知（AC-03）、审批拒绝→200+REJECTED+时间段释放+用户通知（AC-04）、非审批人操作→403。 依赖：T033。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/ApprovalControllerTest.java`。 验证：执行测试，AC-03/AC-04 与权限异常断言通过。 完成判据：测试覆盖 AC-03/AC-04，状态变更与通知创建被验证。 来源：`spec.md#AC-03, AC-04, SCN-05`; `plan.md#3.2`

- [ ] T040 [AC-03] [AC-04] [Phase 3] [Size: M] 实现审批 API 逻辑（ApprovalController + ReservationApplicationService.approve / reject），审批通过：更新状态为 APPROVED + 创建通知；审批拒绝：更新状态为 REJECTED + 释放时间段 + 创建通知。校验审批人权限。 依赖：T039。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/ApprovalController.java`、`backend/src/main/java/com/company/meeting/application/service/ReservationApplicationService.java`（增加 approve / reject 方法）。 验证：执行 T039 测试全部通过。 完成判据：审批逻辑满足 AC-03/AC-04，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-05, AC-03, AC-04`; `plan.md#6.4`

- [ ] T041 [Phase 3] [Size: S] 实现待审批列表 API（GET /api/approvals?page=&size=），返回当前用户作为审批人的待审批预约列表。 依赖：T040。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/ApprovalController.java`（增加 listPending 方法）。 验证：API 测试验证仅返回当前审批人部门的待审批预约。 完成判据：待审批列表仅包含当前审批人负责的预约。 来源：`plan.md#3.2 API 路由`

### 3.6 签到 API

- [ ] T042 [AC-14] [AC-15] [Phase 3] [Size: S] 编写签到 API 的契约/集成测试，覆盖：窗口内签到→200+CHECKED_IN（AC-14）、窗口外签到→400+CHECK_IN_NOT_AVAILABLE（AC-15）、非本人签到→403、已签到再签→400。 依赖：T033。 文件：`backend/src/test/java/com/company/meeting/interfaces/web/ReservationCheckInTest.java`。 验证：执行测试，AC-14/AC-15 与异常路径断言通过。 完成判据：测试覆盖 AC-14/AC-15，签到窗口校验被验证。 来源：`spec.md#AC-14, AC-15, SCN-11`; `plan.md#3.2`

- [ ] T043 [AC-14] [AC-15] [Phase 3] [Size: S] 实现签到 API 逻辑（ReservationController.checkIn + ReservationApplicationService.checkIn），调用 CheckInDomainService 校验窗口 → 更新状态为 CHECKED_IN。 依赖：T042, T021。 文件：`backend/src/main/java/com/company/meeting/application/service/ReservationApplicationService.java`（增加 checkIn 方法）。 验证：执行 T042 测试全部通过。 完成判据：签到逻辑满足 AC-14/AC-15。 来源：`spec.md#SCN-11, AC-14, AC-15`; `plan.md#2.4`

### 3.7 未到场判定定时任务

- [ ] T044 [AC-16] [AC-17] [Phase 3] [Size: S] 编写未到场自动判定定时任务的单元测试，覆盖：会议结束未签到→NO_SHOW+次数+1（AC-16）、累计3次→7天禁令+通知（AC-17）、累计5次→30天禁令+通知。 依赖：T013, T017。 文件：`backend/src/test/java/com/company/meeting/application/scheduler/NoShowDetectionSchedulerTest.java`。 验证：执行测试，AC-16/AC-17 场景断言通过。 完成判据：测试覆盖 AC-16/AC-17，未到场判定与禁令触发被验证。 来源：`spec.md#AC-16, AC-17, SCN-12`; `plan.md#5.3`

- [ ] T045 [AC-16] [AC-17] [Phase 3] [Size: M] 实现未到场自动判定定时任务（NoShowDetectionScheduler），每分钟扫描已结束但未签到的预约，逐条处理：标记 NO_SHOW → 更新用户未到场次数 → 检查禁令 → 创建通知。单条事务处理，避免长事务（ASM-P02）。 依赖：T044。 文件：`backend/src/main/java/com/company/meeting/application/scheduler/NoShowDetectionScheduler.java`。 验证：执行 T044 测试全部通过。 完成判据：定时任务逻辑满足 AC-16/AC-17，事务边界符合 plan.md §6.4。 来源：`spec.md#SCN-12, AC-16, AC-17`; `plan.md#ASM-P02, §6.4`

### 3.8 用户管理与站内信 API

- [ ] T046 [Phase 3] [Size: S] 实现用户管理 API（GET /api/users 分页列表、PUT /api/users/{id} 编辑用户、POST /api/users/{id}/reset-no-show 清零未到场次数），仅管理员可访问。 依赖：T026。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/UserController.java`、`backend/src/main/java/com/company/meeting/application/service/UserApplicationService.java`（增加管理方法）。 验证：API 测试验证管理员可操作、普通用户→403。 完成判据：用户管理 API 满足 SCN-09，权限校验生效。 来源：`spec.md#SCN-09`; `plan.md#3.2`

- [ ] T047 [Phase 3] [Size: S] 实现站内信 API（GET /api/notifications 列表含未读筛选与分页、POST /api/notifications/{id}/read 标记已读、GET /api/notifications/unread-count 未读数）。 依赖：T023。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/NotificationController.java`、`backend/src/main/java/com/company/meeting/application/service/NotificationApplicationService.java`。 验证：API 测试验证列表/已读/未读数功能正确。 完成判据：站内信 API 满足 ASM-P05 简单消息模型。 来源：`spec.md#2 术语表`; `plan.md#3.2, ASM-P05`

### 3.9 统计 API

- [ ] T048 [Phase 3] [Size: S] 实现使用统计 API（GET /api/statistics/usage?startDate=&endDate=&roomId=&groupBy=），基于已通过预约的聚合查询（ASM-P04），仅管理员可访问。 依赖：T023。 文件：`backend/src/main/java/com/company/meeting/interfaces/web/controller/StatisticsController.java`、`backend/src/main/java/com/company/meeting/application/service/StatisticsApplicationService.java`。 验证：API 测试验证按时间/会议室/分组维度的统计结果正确。 完成判据：统计 API 满足 SCN-10，聚合查询符合 ASM-P04。 来源：`spec.md#SCN-10`; `plan.md#3.2, ASM-P04`

---

## Phase 4：前端 API 接入层与类型定义

> 目标：建立前端与后端的类型契约，实现 API Client 调用封装与 Pinia Store。

### 4.1 前端类型定义

- [ ] T049 [Phase 4] [Size: M] 定义前端 TypeScript 类型（models/），与后端 DTO 一一对应：AuthTypes（LoginRequest / RegisterRequest / UserVO）、RoomTypes（RoomCreateRequest / RoomUpdateRequest / RoomVO / AvailabilityVO）、ReservationTypes（ReservationCreateRequest / ReservationUpdateRequest / ReservationVO / ReservationListVO）、ApprovalTypes（ApprovalVO）、NotificationTypes（NotificationVO）、StatisticsTypes（StatisticsQuery / StatisticsVO）、CommonTypes（PageRequest / PageResponse / ErrorResponse）。 依赖：T002。 文件：`frontend/src/models/auth.ts`、`frontend/src/models/room.ts`、`frontend/src/models/reservation.ts`、`frontend/src/models/approval.ts`、`frontend/src/models/notification.ts`、`frontend/src/models/statistics.ts`、`frontend/src/models/common.ts`。 验证：TypeScript 编译通过，类型字段与后端 DTO 逐字段比对一致。 完成判据：前端类型定义与后端 DTO 完全对齐，无遗漏字段。 来源：`plan.md#3.3 类型契约, §3.4 模型映射`

### 4.2 API Service 封装

- [ ] T050 [Phase 4] [Size: M] 实现前端 API Service 层，封装所有后端 API 调用：authService（register / login / logout）、roomService（list / create / update / remove / disable / enable / availability）、reservationService（create / update / cancel / checkIn / listMine / getDetail）、approvalService（listPending / approve / reject）、userService（list / update / resetNoShow）、notificationService（list / markRead / unreadCount）、statisticsService（usage）。 依赖：T049, T007。 文件：`frontend/src/services/authService.ts`、`frontend/src/services/roomService.ts`、`frontend/src/services/reservationService.ts`、`frontend/src/services/approvalService.ts`、`frontend/src/services/userService.ts`、`frontend/src/services/notificationService.ts`、`frontend/src/services/statisticsService.ts`。 验证：TypeScript 编译通过，URL 常量与后端路由一致。 完成判据：API Service 层 URL 与后端 API 路由完全一致，类型签名正确。 来源：`plan.md#3.2 API 路由, §3.3 契约同步`

### 4.3 Pinia Store

- [ ] T051 [AC-12] [Phase 4] [Size: S] 编写 auth Store 的交互测试，验证：登录成功→用户信息存储+路由跳转、登录失败→错误消息展示、登出→清除状态。 依赖：T050。 文件：`frontend/src/stores/__tests__/auth.test.ts`。 验证：执行 Vitest 测试，成功态与错误态均覆盖。 完成判据：测试覆盖登录/登出/错误态，引用 AC-12。 来源：`spec.md#AC-12`; `plan.md#3.1 Store 职责`

- [ ] T052 [AC-12] [Phase 4] [Size: S] 实现 auth Store（Pinia），管理登录态、当前用户信息、登录/登出操作，调用 authService。 依赖：T051。 文件：`frontend/src/stores/auth.ts`。 验证：执行 T051 测试全部通过。 完成判据：auth Store 满足 AC-12，状态管理与 API 调用正确。 来源：`spec.md#SCN-08`; `plan.md#3.1`

- [ ] T053 [Phase 4] [Size: S] 实现 reservation Store（Pinia），管理预约列表、预约详情、提交/修改/取消/签到操作状态，调用 reservationService。 依赖：T050。 文件：`frontend/src/stores/reservation.ts`。 验证：TypeScript 编译通过，Store 方法签名与 API Service 对齐。 完成判据：reservation Store 覆盖预约全部操作，loading/error 状态管理完整。 来源：`plan.md#3.1 Store 职责`

- [ ] T054 [Phase 4] [Size: S] 实现 room Store（Pinia），管理会议室列表、筛选条件、停用/启用操作，调用 roomService。 依赖：T050。 文件：`frontend/src/stores/room.ts`。 验证：TypeScript 编译通过。 完成判据：room Store 覆盖会议室列表与筛选，loading/error 状态管理完整。 来源：`plan.md#3.1 Store 职责`

- [ ] T055 [Phase 4] [Size: S] 实现 user Store（Pinia），管理用户列表（管理员）、编辑操作，调用 userService。 依赖：T050。 文件：`frontend/src/stores/user.ts`。 验证：TypeScript 编译通过。 完成判据：user Store 覆盖用户管理操作。 来源：`plan.md#3.1 Store 职责`

- [ ] T056 [Phase 4] [Size: S] 实现 notification Store（Pinia），管理通知列表、未读数、标记已读，调用 notificationService。 依赖：T050。 文件：`frontend/src/stores/notification.ts`。 验证：TypeScript 编译通过。 完成判据：notification Store 覆盖通知列表与未读数。 来源：`plan.md#3.1 Store 职责`

- [ ] T057 [Phase 4] [Size: S] 实现 statistics Store（Pinia），管理统计数据与筛选条件，调用 statisticsService。 依赖：T050。 文件：`frontend/src/stores/statistics.ts`。 验证：TypeScript 编译通过。 完成判据：statistics Store 覆盖统计查询。 来源：`plan.md#3.1 Store 职责`

### 4.4 路由与鉴权守卫

- [ ] T058 [AC-12] [Phase 4] [Size: S] 实现前端路由配置与鉴权守卫。定义全部路由（plan.md §3.1 页面模块划分），实现 beforeEach 守卫：未登录重定向至 /login，普通用户访问管理员路由重定向至首页。 依赖：T052。 文件：`frontend/src/router/index.ts`。 验证：手动验证未登录访问 /reservations/mine 重定向至 /login；普通用户访问 /admin/rooms 重定向。 完成判据：路由守卫满足 AC-12，权限控制与 plan.md §3.1 一致。 来源：`spec.md#AC-12`; `plan.md#3.1 页面与前端架构`

---

## Phase 5：前端主链路页面与交互

> 目标：实现全部页面组件，完成核心用户操作流程的前端交互。

### 5.1 认证页面

- [ ] T059 [AC-12] [Phase 5] [Size: S] 实现登录页面（/login），含用户名/密码表单、提交调用 auth Store、错误提示、登录成功跳转至首页。 依赖：T052, T058。 文件：`frontend/src/views/auth/LoginView.vue`。 验证：手动验证登录成功跳转、密码错误 Toast 提示。 完成判据：登录页面满足 AC-12，表单校验与错误反馈完整。 来源：`spec.md#SCN-08, AC-12`; `plan.md#3.1`

- [ ] T060 [Phase 5] [Size: S] 实现注册页面（/register），含用户名/密码/部门选择表单、提交调用 auth Store、注册成功跳转至登录页。 依赖：T052, T058。 文件：`frontend/src/views/auth/RegisterView.vue`。 验证：手动验证注册成功跳转、用户名重复 Toast 提示。 完成判据：注册页面功能完整，错误反馈符合后端异常响应。 来源：`spec.md#SCN-08`; `plan.md#3.1`

### 5.2 会议室与预约页面

- [ ] T061 [Phase 5] [Size: M] 实现会议室列表页面（/rooms），含筛选条件（容量/设备/状态）、分页、可用时间段查询、预约按钮跳转。 依赖：T054, T058。 文件：`frontend/src/views/room/RoomListView.vue`。 验证：手动验证筛选、分页、可用时间段展示、预约跳转。 完成判据：会议室列表页面功能完整，筛选与分页正确。 来源：`spec.md#SCN-01`; `plan.md#3.1`

- [ ] T062 [AC-01] [AC-02] [AC-09] [AC-10] [AC-18] [Phase 5] [Size: M] 实现预约提交页面（/reservations/new），含会议室选择/时间段选择/会议名称/参会人数表单，提交调用 reservation Store，冲突/时间约束/禁令错误 Toast 提示。 依赖：T053, T061。 文件：`frontend/src/views/reservation/ReservationCreateView.vue`。 验证：手动验证预约成功跳转、冲突提示、时间约束提示、禁令提示。 完成判据：预约提交页面满足 AC-01/AC-02/AC-09/AC-10/AC-18 的前端交互。 来源：`spec.md#SCN-01, AC-01, AC-02, AC-09, AC-10, AC-18`; `plan.md#3.1`

- [ ] T063 [AC-05] [AC-06] [AC-07] [AC-08] [Phase 5] [Size: M] 实现我的预约列表页面（/reservations/mine），含状态筛选/分页，预约卡片含取消/修改/签到按钮（按状态显示），取消确认弹窗，修改跳转至详情页。 依赖：T053, T058。 文件：`frontend/src/views/reservation/MyReservationView.vue`。 验证：手动验证列表展示、状态筛选、取消确认、修改跳转。 完成判据：我的预约列表满足 AC-05/AC-06 的前端交互。 来源：`spec.md#SCN-02, SCN-03`; `plan.md#3.1`

- [ ] T064 [AC-06] [AC-07] [AC-08] [AC-14] [AC-15] [Phase 5] [Size: M] 实现预约详情/修改页面（/reservations/:id），含预约信息展示、修改表单（仅 PENDING/APPROVED/REJECTED 状态可修改）、签到按钮（仅 APPROVED 且在窗口内可签到），修改/签到错误 Toast 提示。 依赖：T053, T058。 文件：`frontend/src/views/reservation/ReservationDetailView.vue`。 验证：手动验证修改成功重置为待审批、冲突提示、签到成功/窗口外提示。 完成判据：预约详情页满足 AC-06/AC-07/AC-08/AC-14/AC-15 的前端交互。 来源：`spec.md#SCN-03, SCN-04, SCN-11`; `plan.md#3.1`

### 5.3 审批页面

- [ ] T065 [AC-03] [AC-04] [Phase 5] [Size: S] 实现待审批列表页面（/approvals），含待审批预约列表、通过/拒绝按钮、拒绝原因输入弹窗，操作后刷新列表。 依赖：T053, T058。 文件：`frontend/src/views/reservation/ApprovalView.vue`。 验证：手动验证审批通过/拒绝后列表刷新、用户通知。 完成判据：审批页面满足 AC-03/AC-04 的前端交互。 来源：`spec.md#SCN-05, AC-03, AC-04`; `plan.md#3.1`

### 5.4 管理员页面

- [ ] T066 [Phase 5] [Size: M] 实现会议室管理页面（/admin/rooms），含会议室列表（含状态标识）、新增/编辑弹窗、删除确认、停用/启用按钮，停用时确认弹窗提示将取消关联预约。 依赖：T054, T058。 文件：`frontend/src/views/room/RoomManageView.vue`。 验证：手动验证新增/编辑/删除/停用/启用操作。 完成判据：会议室管理页面满足 SCN-06/SCN-07 的前端交互。 来源：`spec.md#SCN-06, SCN-07`; `plan.md#3.1`

- [ ] T067 [Phase 5] [Size: S] 实现用户管理页面（/admin/users），含用户列表、编辑角色/部门弹窗、清零未到场次数按钮。 依赖：T055, T058。 文件：`frontend/src/views/user/UserManageView.vue`。 验证：手动验证编辑用户、清零未到场次数操作。 完成判据：用户管理页面满足 SCN-09 的前端交互。 来源：`spec.md#SCN-09`; `plan.md#3.1`

- [ ] T068 [Phase 5] [Size: S] 实现使用统计页面（/admin/statistics），含时间范围/会议室/分组维度筛选，统计图表展示。 依赖：T057, T058。 文件：`frontend/src/views/statistics/StatisticsView.vue`。 验证：手动验证筛选条件切换、统计数据展示。 完成判据：统计页面满足 SCN-10 的前端交互。 来源：`spec.md#SCN-10`; `plan.md#3.1`

### 5.5 通知页面

- [ ] T069 [Phase 5] [Size: S] 实现站内信列表页面（/notifications），含未读/全部筛选、标记已读、未读数角标（导航栏），点击通知跳转关联预约详情。 依赖：T056, T058。 文件：`frontend/src/views/notification/NotificationView.vue`。 验证：手动验证未读筛选、标记已读、导航栏未读数更新。 完成判据：站内信页面功能完整，未读数实时更新。 来源：`spec.md#2 术语表`; `plan.md#3.1`

### 5.6 通用组件与布局

- [ ] T070 [Phase 5] [Size: S] 实现前端通用布局组件：AppLayout（含顶部导航栏+侧边菜单+内容区）、导航栏含未读通知角标、侧边菜单按角色显示（普通用户/管理员）。 依赖：T058, T056。 文件：`frontend/src/components/AppLayout.vue`、`frontend/src/components/AppHeader.vue`、`frontend/src/components/AppSidebar.vue`。 验证：手动验证布局展示、角色菜单差异、通知角标。 完成判据：布局组件正确，角色菜单与 plan.md §3.1 一致。 来源：`plan.md#3.1 页面与前端架构`

- [ ] T071 [Phase 5] [Size: S] 实现前端通用状态组件：Loading 指令/组件、Empty 占位组件、Confirm 确认弹窗组件，统一加载/空态/确认交互。 依赖：T002。 文件：`frontend/src/components/LoadingSpinner.vue`、`frontend/src/components/EmptyState.vue`、`frontend/src/components/ConfirmDialog.vue`。 验证：手动验证各组件展示效果。 完成判据：通用组件符合 plan.md §3.1 加载/异常态策略。 来源：`plan.md#3.1 加载/异常态策略`

---

## Phase 6：联调、鉴权、异常与契约补强

> 目标：前后端联调，补强跨层异常处理、权限控制、类型与契约同步、日志。

- [ ] T072 [AC-12] [Phase 6] [Size: M] 前后端联调认证流程：验证注册→登录→Session 保持→登出→未登录重定向全链路，确认 Cookie/Session 机制在前后端代理环境下正常工作。 依赖：T059, T060, T026。 文件：`frontend/vite.config.ts`（代理配置确认）、`backend/src/main/java/com/company/meeting/infrastructure/security/SecurityConfig.java`。 验证：手动验证完整认证流程。 完成判据：认证全链路通过，Session 在 Vite 代理下正常传递。 来源：`spec.md#AC-12`; `plan.md#3.3 环境与配置`

- [ ] T073 [AC-01] [AC-02] [AC-09] [Phase 6] [Size: M] 前后端联调预约提交流程：验证无冲突预约→成功→审批人收到通知、冲突预约→Toast 提示、时间约束违规→Toast 提示、停用室预约→Toast 提示、禁令期间预约→Toast 提示。 依赖：T062, T033。 文件：无需新增文件，修复联调问题。 验证：手动验证全部预约提交场景。 完成判据：预约提交全链路通过，错误反馈与后端错误码一致。 来源：`spec.md#AC-01, AC-02, AC-09, AC-10, AC-18`; `plan.md#3.3 异常拦截, 错误反馈`

- [ ] T074 [AC-03] [AC-04] [AC-05] [AC-06] [AC-07] [AC-08] [Phase 6] [Size: M] 前后端联调预约管理流程：验证审批通过/拒绝→状态变更+通知、取消预约→状态变更+通知、修改预约→冲突检测+状态重置、被拒绝预约修改重提。 依赖：T063, T064, T065, T040, T035, T037。 文件：无需新增文件，修复联调问题。 验证：手动验证全部预约管理场景。 完成判据：预约管理全链路通过，状态变更与通知正确。 来源：`spec.md#AC-03~AC-08`; `plan.md#3.3`

- [ ] T075 [AC-14] [AC-15] [AC-16] [AC-17] [Phase 6] [Size: M] 前后端联调签到与未到场流程：验证窗口内签到→成功、窗口外签到→提示、等待定时任务触发→未到场判定→禁令触发→禁令期间预约被拒。 依赖：T064, T043, T045。 文件：无需新增文件，修复联调问题。 验证：手动验证签到与未到场全链路（可调整定时任务间隔加速测试）。 完成判据：签到与未到场全链路通过，禁令触发正确。 来源：`spec.md#AC-14~AC-17`; `plan.md#3.3`

- [ ] T076 [AC-10] [AC-11] [AC-13] [Phase 6] [Size: S] 前后端联调会议室停用/启用流程：验证停用→关联预约取消+通知、停用后预约被拒、启用→恢复可预约。 依赖：T066, T030。 文件：无需新增文件，修复联调问题。 验证：手动验证停用/启用全链路。 完成判据：停用/启用全链路通过，关联预约取消与通知正确。 来源：`spec.md#AC-10, AC-11, AC-13`; `plan.md#3.3`

- [ ] T077 [Phase 6] [Size: S] 补强后端日志：在关键操作（预约提交/审批/签到/禁令触发）添加 INFO 日志，含预约ID/用户ID；配置请求追踪 Filter（X-Request-Id + MDC 注入）。 依赖：T033, T040, T043, T045。 文件：`backend/src/main/java/com/company/meeting/infrastructure/config/RequestIdFilter.java`、`backend/src/main/resources/logback-spring.xml`。 验证：手动触发操作，检查日志输出含 RequestId 与业务ID。 完成判据：日志策略符合 plan.md §4.3 可观测性。 来源：`plan.md#4.3 可观测性`

- [ ] T078 [Phase 6] [Size: S] 配置 Spring Boot Actuator 健康检查端点（/actuator/health），确认数据库连接检查生效。 依赖：T001。 文件：`backend/src/main/resources/application.yml`（增加 actuator 配置）。 验证：访问 /actuator/health 返回 UP 状态。 完成判据：健康检查端点可用，符合 plan.md §4.3。 来源：`plan.md#4.3 可观测性`

- [ ] T079 [Phase 6] [Size: S] 前后端类型契约同步审查：逐字段比对前端 `models/` 与后端 `dto/` 的类型定义，修复不一致项；确认 API Service URL 常量与后端路由一致。 依赖：T049, T050, T033, T040。 文件：`frontend/src/models/`、`frontend/src/services/`。 验证：人工逐字段比对，无遗漏或不一致。 完成判据：前后端类型定义完全一致，URL 常量无偏差。 来源：`plan.md#3.3 类型契约, 契约同步`

---

## Phase 7：E2E 测试、可观测性与收尾

> 目标：端到端回归验证、文档与发布前检查。

- [ ] T080 [AC-01] [AC-02] [AC-03] [AC-04] [AC-05] [AC-06] [AC-07] [AC-08] [AC-09] [AC-10] [AC-11] [AC-12] [AC-13] [AC-14] [AC-15] [AC-16] [AC-17] [AC-18] [Phase 7] [Size: M] 执行端到端回归验证，按 AC-01~AC-18 逐条手动验证核心流程：注册→登录→预约→审批→签到→未到场→禁令→停用/启用，记录每条 AC 的验证结果。 依赖：T072~T076。 文件：无新增文件，输出验证报告。 验证：逐条 AC 验证通过。 完成判据：18 条 AC 全部验证通过，无阻塞性缺陷。 来源：`spec.md#7 验收标准`; `plan.md#5.3 验收场景映射`

- [ ] T081 [Phase 7] [Size: S] 配置后端多环境支持：`application-dev.yml`（开发环境，H2 可选）、`application-prod.yml`（生产环境，MySQL），环境变量覆盖机制。 依赖：T001。 文件：`backend/src/main/resources/application-dev.yml`、`backend/src/main/resources/application-prod.yml`。 验证：使用 `--spring.profiles.active=dev` 启动成功。 完成判据：多环境配置符合 plan.md §4.2。 来源：`plan.md#4.2 配置与环境`

- [ ] T082 [Phase 7] [Size: S] 前端生产构建与部署配置：`npm run build` 成功，`.env.production` 配置生产 API 地址，构建产物可部署。 依赖：T079。 文件：`frontend/.env.production`、`frontend/vite.config.ts`。 验证：`npm run build` 成功，产物可静态部署。 完成判据：前端生产构建成功，无编译错误。 来源：`plan.md#4.2 配置与环境`

- [ ] T083 [Phase 7] [Size: S] 发布前检查清单：确认所有 AC 验证通过、数据库迁移脚本可重复执行、前后端类型契约一致、日志与健康检查可用、无已知阻塞性缺陷。 依赖：T080, T077, T078, T079, T081, T082。 文件：无新增文件，输出检查报告。 验证：逐项检查确认。 完成判据：发布前检查清单全部通过。 来源：`plan.md#6.1 分阶段建议`

---

## 任务统计

| 阶段 | 任务数 | S | M | 关键产出 |
|------|--------|---|---|----------|
| Phase 1 基础骨架 | 7 | 4 | 3 | 后端/前端项目骨架、异常处理、Result 模式、DDL |
| Phase 2 领域模型 | 15 | 10 | 5 | 领域实体/值对象/枚举/领域服务/Gateway 接口 + 表格驱动测试 |
| Phase 3 应用层与后端 | 26 | 12 | 14 | Application Service / Controller / DTO / 认证 / 定时任务 + API 集成测试 |
| Phase 4 前端 API 接入 | 10 | 7 | 3 | TypeScript 类型 / API Service / Pinia Store / 路由守卫 |
| Phase 5 前端页面 | 13 | 5 | 8 | 全部页面组件 + 通用布局/状态组件 |
| Phase 6 联调补强 | 8 | 4 | 4 | 前后端联调 + 日志 + 健康检查 + 契约同步 |
| Phase 7 收尾 | 4 | 3 | 1 | E2E 回归 + 多环境 + 构建部署 + 发布检查 |
| **合计** | **83** | **45** | **38** | |

---

## AC 追踪矩阵

| AC | 覆盖任务（测试先行） | 覆盖任务（实现） | 覆盖任务（联调/验证） |
|----|---------------------|-----------------|---------------------|
| AC-01 | T032 | T033 | T073 |
| AC-02 | T011, T019, T032 | T018, T033 | T073 |
| AC-03 | T039 | T040 | T074 |
| AC-04 | T039 | T040 | T074 |
| AC-05 | T034 | T035 | T074 |
| AC-06 | T036 | T037 | T074 |
| AC-07 | T036 | T037 | T074 |
| AC-08 | T036 | T037 | T074 |
| AC-09 | T008, T032 | T009, T033 | T073 |
| AC-10 | T029, T032 | T030, T033 | T076 |
| AC-11 | T029 | T030 | T076 |
| AC-12 | T025, T051 | T026, T052 | T072 |
| AC-13 | T029 | T030 | T076 |
| AC-14 | T020, T042 | T021, T043 | T075 |
| AC-15 | T020, T042 | T021, T043 | T075 |
| AC-16 | T044 | T045 | T075 |
| AC-17 | T014, T044 | T013, T045 | T075 |
| AC-18 | T014, T032 | T013, T033 | T073 |

---

## INV 追踪矩阵

| INV | 守护位置（领域层） | 守护位置（兜底） | 测试任务 |
|-----|-------------------|-----------------|----------|
| INV-01 | ReservationDomainService | 数据库唯一约束 (T006) | T011, T019 |
| INV-02 | Reservation 状态机 | - | T039 |
| INV-03 | Application 层编排 | - | T036 |
| INV-04 | ReservationDomainService | - | T018, T029 |
| INV-05 | TimeSlot 自校验 | - | T008 |
| INV-06 | NoShowDetectionScheduler | - | T044 |
| INV-07 | ReservationDomainService + User | - | T014, T018 |
