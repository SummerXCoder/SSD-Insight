package com.company.meeting.domain.types;

/**
 * 会议室状态枚举 — 会议室的可用性状态
 * <p>
 * 来源：spec.md#2 术语表(停用); plan.md#2.1 核心领域概念
 */
public enum RoomStatus {

    /**
     * 启用 — 会议室可被预约
     * Implements: spec.md#SCN-07, AC-13
     */
    ENABLED,

    /**
     * 停用 — 会议室因维护等原因暂时不可预约
     * Implements: spec.md#SCN-07, INV-04, AC-10
     */
    DISABLED
}
