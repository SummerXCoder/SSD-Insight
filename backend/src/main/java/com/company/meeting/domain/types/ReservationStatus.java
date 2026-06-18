package com.company.meeting.domain.types;

/**
 * 预约状态枚举 — 预约生命周期的状态流转
 * <p>
 * 状态流转规则（spec.md §5, plan.md §2.2）：
 * PENDING → APPROVED / REJECTED / CANCELLED
 * REJECTED → PENDING（修改后重新提交）
 * APPROVED → CANCELLED / CHECKED_IN / NO_SHOW
 * <p>
 * 来源：spec.md#2 术语表(预约); plan.md#2.1 核心领域概念, §2.2 状态流转
 */
public enum ReservationStatus {

    /**
     * 待审批 — 用户提交预约后的初始状态
     * Implements: spec.md#SCN-01, INV-02
     */
    PENDING,

    /**
     * 已通过 — 审批人通过审批后的状态
     * Implements: spec.md#SCN-05, INV-02
     */
    APPROVED,

    /**
     * 已拒绝 — 审批人拒绝审批后的状态，时间段释放
     * Implements: spec.md#SCN-05
     */
    REJECTED,

    /**
     * 已取消 — 用户主动取消预约后的状态，时间段释放
     * Implements: spec.md#SCN-02
     */
    CANCELLED,

    /**
     * 已签到 — 预约人在签到窗口内完成签到
     * Implements: spec.md#SCN-11, AC-14
     */
    CHECKED_IN,

    /**
     * 未到场 — 会议结束后预约人未签到，系统自动判定
     * Implements: spec.md#SCN-12, INV-06, AC-16
     */
    NO_SHOW
}
