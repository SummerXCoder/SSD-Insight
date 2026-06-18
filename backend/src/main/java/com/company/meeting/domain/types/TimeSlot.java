package com.company.meeting.domain.types;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 时间段值对象 — 预约的起止时间范围，用于冲突检测与时间约束校验
 * <p>
 * 来源：spec.md#2 术语表(时间段); plan.md#2.1 核心领域概念, §5.2 核心逻辑 2
 * <p>
 * T009 已实现完整校验逻辑。
 */
public class TimeSlot {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 自校验方法：校验 INV-05 时间约束
     * - 提前量 ≥ 1小时
     * - 时长 ≤ 2小时
     * - 范围 ≤ 14天
     * <p>
     * Implements: spec.md#INV-05, AC-09; plan.md#5.2 核心逻辑 2
     *
     * @param now 当前时间
     * @return 校验结果
     */
    public Result<Void> validateConstraints(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");

        // Implements: INV-05, AC-09 — 提前量 ≥ 1小时
        if (startTime.isBefore(now.plusHours(1))) {
            return Result.failure("TIME_CONSTRAINT_VIOLATION",
                    "预约开始时间必须至少提前1小时");
        }

        // Implements: INV-05, AC-09 — 时长 ≤ 2小时
        if (endTime.isAfter(startTime.plusHours(2))) {
            return Result.failure("TIME_CONSTRAINT_VIOLATION",
                    "预约时长不得超过2小时");
        }

        // Implements: INV-05, AC-09 — 范围 ≤ 14天
        if (startTime.isAfter(now.plusDays(14))) {
            return Result.failure("TIME_CONSTRAINT_VIOLATION",
                    "预约时间范围不得超过14天");
        }

        return Result.success();
    }
}
