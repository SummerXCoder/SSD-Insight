-- ============================================================================
-- data-init.sql — 企业级会议室预约系统 初始数据
-- Implements: T006 [Phase 1] — 初始部门与管理员数据
-- 来源: plan.md §2.1, spec.md §3 (管理员同时作为部门审批人)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 初始部门数据
-- 说明: 创建3个示例部门，每个部门指定审批人(管理员)
-- ---------------------------------------------------------------------------
INSERT INTO department (id, name, approver_id) VALUES
    (1, '技术研发部', NULL),
    (2, '产品设计部', NULL),
    (3, '市场运营部', NULL);

-- ---------------------------------------------------------------------------
-- 初始管理员数据
-- 说明: 创建3个管理员用户，分别作为3个部门的审批人
-- 密码均为 BCrypt 哈希后的 "admin123" (ASM-P01)
-- BCrypt hash for "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ---------------------------------------------------------------------------
INSERT INTO user (id, username, password, role, department_id, no_show_count, ban_deadline) VALUES
    (1, 'admin_tech',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1, 0, NULL),
    (2, 'admin_product', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 2, 0, NULL),
    (3, 'admin_market', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 3, 0, NULL);

-- ---------------------------------------------------------------------------
-- 回填部门审批人
-- 说明: 管理员创建后，更新部门的 approver_id
-- ---------------------------------------------------------------------------
UPDATE department SET approver_id = 1 WHERE id = 1;
UPDATE department SET approver_id = 2 WHERE id = 2;
UPDATE department SET approver_id = 3 WHERE id = 3;

-- ---------------------------------------------------------------------------
-- 初始会议室数据
-- 说明: 创建3间示例会议室，覆盖不同容量与设备组合
-- ---------------------------------------------------------------------------
INSERT INTO meeting_room (id, name, location, capacity, equipment_list, status) VALUES
    (1, '朝阳厅', 'A栋3楼301', 20, 'PROJECTOR,WHITEBOARD', 'ENABLED'),
    (2, '星光厅', 'A栋5楼502', 10, 'WHITEBOARD,VIDEO_CONF', 'ENABLED'),
    (3, '银河厅', 'B栋2楼201', 50, 'PROJECTOR,WHITEBOARD,VIDEO_CONF', 'ENABLED');
