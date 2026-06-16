-- ============================================================================
-- schema.sql — 企业级会议室预约系统 数据库表结构
-- Implements: T006 [Phase 1] — 设计并创建 MySQL 数据库表结构
-- 来源: plan.md §2.1 领域模型, §6.3 并发控制策略
-- 引用: spec.md §2 术语表, INV-01 (同室同时段无冲突兜底)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 部门表 (Department)
-- spec.md §2: 部门 — 扁平单层组织单元，关联审批人
-- plan.md §2.1: Department 实体，含 id / name / approverId
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS department (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    name        VARCHAR(100)    NOT NULL COMMENT '部门名称',
    approver_id BIGINT          DEFAULT NULL COMMENT '审批人(管理员)用户ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_name (name),
    KEY idx_department_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ---------------------------------------------------------------------------
-- 用户表 (User)
-- spec.md §2: 用户 — 系统使用者，分为普通用户和管理员
-- plan.md §2.1: User 实体，含 id / username / password / role / departmentId / noShowCount / reservationBan
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user (
    id                BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username          VARCHAR(50)     NOT NULL COMMENT '用户名',
    password          VARCHAR(255)    NOT NULL COMMENT '密码(BCrypt哈希, ASM-P01)',
    role              VARCHAR(20)     NOT NULL DEFAULT 'REGULAR' COMMENT '角色: REGULAR/ADMIN',
    department_id     BIGINT          DEFAULT NULL COMMENT '所属部门ID',
    no_show_count     INT             NOT NULL DEFAULT 0 COMMENT '累计未到场次数',
    ban_deadline      DATETIME        DEFAULT NULL COMMENT '预约禁令截止时间, NULL表示无禁令',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_department_id (department_id),
    KEY idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ---------------------------------------------------------------------------
-- 会议室表 (MeetingRoom)
-- spec.md §2: 会议室 — 可被预约的共享空间资源，含容量、设备、位置等属性
-- plan.md §2.1: MeetingRoom 实体，含 id / name / location / capacity / equipmentList / status
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meeting_room (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '会议室ID',
    name            VARCHAR(100)    NOT NULL COMMENT '会议室名称',
    location        VARCHAR(200)    DEFAULT NULL COMMENT '位置',
    capacity        INT             NOT NULL DEFAULT 0 COMMENT '容量(可容纳人数)',
    equipment_list  VARCHAR(500)    DEFAULT NULL COMMENT '设备列表(逗号分隔枚举值: PROJECTOR,WHITEBOARD,VIDEO_CONF等)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT '状态: ENABLED/DISABLED',
    PRIMARY KEY (id),
    UNIQUE KEY uk_meeting_room_name (name),
    KEY idx_meeting_room_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';

-- ---------------------------------------------------------------------------
-- 预约表 (Reservation)
-- spec.md §2: 预约 — 用户对某间会议室在特定时间段的占用申请
-- plan.md §2.1: Reservation 聚合根，含 id / roomId / userId / timeSlot / status / meetingName / attendeeCount
-- plan.md §6.3: 乐观锁 + 数据库排他查询, 唯一约束(room_id + 日期范围)作为INV-01兜底
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservation (
    id                BIGINT          NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    room_id           BIGINT          NOT NULL COMMENT '会议室ID',
    user_id           BIGINT          NOT NULL COMMENT '预约人用户ID',
    start_time        DATETIME        NOT NULL COMMENT '预约开始时间',
    end_time          DATETIME        NOT NULL COMMENT '预约结束时间',
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED/CANCELLED/CHECKED_IN/NO_SHOW',
    meeting_name      VARCHAR(200)    NOT NULL COMMENT '会议名称',
    attendee_count    INT             NOT NULL DEFAULT 0 COMMENT '参会人数',
    version           INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_reservation_room_id_date (room_id, start_time),
    KEY idx_reservation_user_id (user_id),
    KEY idx_reservation_status (status),
    KEY idx_reservation_room_status_start (room_id, status, start_time),
    CONSTRAINT fk_reservation_room_id FOREIGN KEY (room_id) REFERENCES meeting_room(id),
    CONSTRAINT fk_reservation_user_id FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ---------------------------------------------------------------------------
-- 站内信表 (Notification)
-- spec.md §2: 站内信 — 系统内部的消息通知机制
-- plan.md §2.1: Notification 实体，含 id / userId / content / read / reservationId / createdAt
-- ASM-P05: 简单消息模型，仅支持未读/已读状态
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id         BIGINT          NOT NULL COMMENT '接收人用户ID',
    content         VARCHAR(500)    NOT NULL COMMENT '通知内容',
    is_read         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    reservation_id  BIGINT          DEFAULT NULL COMMENT '关联预约ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_notification_user_id_read (user_id, is_read),
    KEY idx_notification_user_id (user_id),
    CONSTRAINT fk_notification_user_id FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_notification_reservation_id FOREIGN KEY (reservation_id) REFERENCES reservation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信表';
