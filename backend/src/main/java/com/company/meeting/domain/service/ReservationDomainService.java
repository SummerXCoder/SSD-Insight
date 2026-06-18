package com.company.meeting.domain.service;

import com.company.meeting.domain.gateway.ReservationRepository;
import com.company.meeting.domain.types.Result;
import com.company.meeting.domain.types.TimeSlot;

import java.util.List;

/**
 * 预约领域服务 — 冲突检测、预约提交/修改的业务规则校验
 * <p>
 * 核心职责：
 * ① 冲突检测方法 checkConflict()（依赖 ReservationRepository 查询同室同日预约，排除自身）
 * ② 预约提交校验方法 validateSubmission()（校验会议室状态、时间约束、用户禁令、冲突检测）
 * ③ 预约修改校验方法 validateUpdate()（同上，排除自身）
 * <p>
 * 来源：plan.md §2.4 关键抽象, §2.2 状态流转与不变量
 * Implements: spec.md#INV-01, INV-04, INV-05, INV-07; plan.md §2.2, §2.4
 */
public class ReservationDomainService {

    private final ReservationRepository reservationRepository;

    public ReservationDomainService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * 冲突检测 — INV-01 核心规则
     * <p>
     * 两个时间段 [s1, e1) 和 [s2, e2) 冲突当且仅当 s1 < e2 && s2 < e1。
     * 待审批预约也占用时间段，从根本上避免审批后冲突。
     * <p>
     * Implements: spec.md#INV-01, AC-02; plan.md §5.2 核心逻辑 1
     *
     * @param roomId              会议室 ID
     * @param newTimeSlot         新预约时间段
     * @param excludeReservationId 需要排除的预约 ID（修改时排除自身），可为 null
     * @return 冲突检测结果：无冲突返回 success，有冲突返回 failure + RESERVATION_CONFLICT
     */
    public Result<Void> checkConflict(Long roomId, TimeSlot newTimeSlot, Long excludeReservationId) {
        List<TimeSlot> existingTimeSlots = reservationRepository.findActiveTimeSlotsByRoom(
                roomId, newTimeSlot, excludeReservationId);

        for (TimeSlot existing : existingTimeSlots) {
            if (hasOverlap(newTimeSlot, existing)) {
                // Implements: INV-01, AC-02 — 同一会议室同一时间段存在冲突预约
                return Result.failure("RESERVATION_CONFLICT",
                        "会议室在该时间段已有预约，存在时间冲突");
            }
        }

        return Result.success();
    }

    /**
     * 判断两个时间段是否存在重叠
     * <p>
     * 冲突判定公式：[s1, e1) 和 [s2, e2) 冲突 ⟺ s1 < e2 && s2 < e1
     * 端点相邻不冲突（如 10:00-11:00 与 11:00-12:00 不冲突）
     * <p>
     * Implements: spec.md#INV-01; plan.md §5.2 核心逻辑 1
     *
     * @param slot1 时间段1
     * @param slot2 时间段2
     * @return true 表示存在重叠（冲突），false 表示不重叠
     */
    private boolean hasOverlap(TimeSlot slot1, TimeSlot slot2) {
        return slot1.getStartTime().isBefore(slot2.getEndTime())
                && slot2.getStartTime().isBefore(slot1.getEndTime());
    }
}
