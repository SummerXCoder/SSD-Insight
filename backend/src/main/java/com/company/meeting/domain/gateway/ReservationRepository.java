package com.company.meeting.domain.gateway;

import com.company.meeting.domain.types.TimeSlot;

import java.util.List;

/**
 * 预约仓储接口 — 预约持久化与查询（含冲突检测查询）
 * <p>
 * 依赖倒置：Domain 层定义接口，Infrastructure 层提供 MyBatis 实现。
 * <p>
 * 来源：plan.md §2.4 关键抽象, §2.3 系统结构建议
 */
public interface ReservationRepository {

    /**
     * 查询指定会议室在指定日期范围内的有效预约（PENDING / APPROVED）的时间段列表
     * <p>
     * 用于冲突检测：INV-01 同一会议室同一时间段不可存在两条"已通过"或"待审批"的预约
     * <p>
     * Implements: spec.md#INV-01, AC-02; plan.md §2.4 关键抽象
     *
     * @param roomId   会议室 ID
     * @param dateRange 日期范围（查询该范围内的预约时间段）
     * @param excludeReservationId 需要排除的预约 ID（修改预约时排除自身），可为 null
     * @return 有效预约的时间段列表
     */
    List<TimeSlot> findActiveTimeSlotsByRoom(Long roomId, TimeSlot dateRange, Long excludeReservationId);
}
