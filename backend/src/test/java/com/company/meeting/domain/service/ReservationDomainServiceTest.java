package com.company.meeting.domain.service;

import com.company.meeting.domain.gateway.ReservationRepository;
import com.company.meeting.domain.types.Result;
import com.company.meeting.domain.types.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T011: 冲突检测单元测试
 * <p>
 * 覆盖 plan.md §5.2 核心逻辑 1（冲突检测 INV-01）全部 8 个场景：
 * 1. 端点相邻不冲突（新预约紧接已有预约之后）
 * 2. 端点相邻不冲突（新预约紧接已有预约之前）
 * 3. 部分重叠（新预约开始时间在已有预约内）
 * 4. 部分重叠（新预约结束时间在已有预约内）
 * 5. 完全包含（新预约完全覆盖已有预约）
 * 6. 完全相同（新预约与已有预约时间段一致）
 * 7. 完全不重叠（新预约与已有预约无交集）
 * 8. 无已有预约（无冲突）
 * <p>
 * 测试命名引用 AC-02，业务不变量引用 INV-01。
 * 来源：spec.md#INV-01, AC-02; plan.md#5.2 核心逻辑 1
 */
class ReservationDomainServiceTest {

    /**
     * 表格驱动测试数据：plan.md §5.2 核心逻辑 1
     * <p>
     * | # | 已有预约时间段 | 新预约时间段 | 是否冲突 | 说明 |
     * |---|---------------|-------------|----------|------|
     * | 1 | 10:00-11:00   | 11:00-12:00 | 否       | 端点相邻不冲突 |
     * | 2 | 10:00-11:00   | 09:00-10:00 | 否       | 端点相邻不冲突 |
     * | 3 | 10:00-11:00   | 10:30-11:30 | 是       | 部分重叠 |
     * | 4 | 10:00-11:00   | 09:30-10:30 | 是       | 部分重叠 |
     * | 5 | 10:00-11:00   | 09:00-12:00 | 是       | 完全包含 |
     * | 6 | 10:00-11:00   | 10:00-11:00 | 是       | 完全相同 |
     * | 7 | 10:00-11:00   | 08:00-09:00 | 否       | 完全不重叠 |
     * | 8 | 无            | 10:00-11:00 | 否       | 无已有预约 |
     */
    record ConflictScenario(
            String description,
            TimeSlot existingSlot,
            TimeSlot newSlot,
            boolean hasConflict,
            int scenarioNumber
    ) {}

    static Stream<ConflictScenario> conflictScenarios() {
        LocalDateTime base = LocalDateTime.of(2026, 6, 18, 0, 0);
        TimeSlot existing = new TimeSlot(base.withHour(10), base.withHour(11));

        return Stream.of(
                // 场景1: 端点相邻不冲突 — 新预约紧接已有预约之后
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景1
                new ConflictScenario(
                        "场景1: 端点相邻不冲突 — 新预约11:00-12:00紧接已有10:00-11:00之后",
                        existing,
                        new TimeSlot(base.withHour(11), base.withHour(12)),
                        false,
                        1
                ),
                // 场景2: 端点相邻不冲突 — 新预约紧接已有预约之前
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景2
                new ConflictScenario(
                        "场景2: 端点相邻不冲突 — 新预约09:00-10:00紧接已有10:00-11:00之前",
                        existing,
                        new TimeSlot(base.withHour(9), base.withHour(10)),
                        false,
                        2
                ),
                // 场景3: 部分重叠 — 新预约开始时间在已有预约内
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景3
                new ConflictScenario(
                        "场景3: 部分重叠 — 新预约10:30-11:30与已有10:00-11:00重叠",
                        existing,
                        new TimeSlot(base.withHour(10).withMinute(30), base.withHour(11).withMinute(30)),
                        true,
                        3
                ),
                // 场景4: 部分重叠 — 新预约结束时间在已有预约内
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景4
                new ConflictScenario(
                        "场景4: 部分重叠 — 新预约09:30-10:30与已有10:00-11:00重叠",
                        existing,
                        new TimeSlot(base.withHour(9).withMinute(30), base.withHour(10).withMinute(30)),
                        true,
                        4
                ),
                // 场景5: 完全包含 — 新预约完全覆盖已有预约
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景5
                new ConflictScenario(
                        "场景5: 完全包含 — 新预约09:00-12:00完全覆盖已有10:00-11:00",
                        existing,
                        new TimeSlot(base.withHour(9), base.withHour(12)),
                        true,
                        5
                ),
                // 场景6: 完全相同 — 新预约与已有预约时间段一致
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景6
                new ConflictScenario(
                        "场景6: 完全相同 — 新预约10:00-11:00与已有10:00-11:00完全一致",
                        existing,
                        new TimeSlot(base.withHour(10), base.withHour(11)),
                        true,
                        6
                ),
                // 场景7: 完全不重叠 — 新预约与已有预约无交集
                // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景7
                new ConflictScenario(
                        "场景7: 完全不重叠 — 新预约08:00-09:00与已有10:00-11:00无交集",
                        existing,
                        new TimeSlot(base.withHour(8), base.withHour(9)),
                        false,
                        7
                )
        );
    }

    @Nested
    @DisplayName("INV-01 冲突检测 — plan.md §5.2 核心逻辑 1")
    class ConflictDetectionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.company.meeting.domain.service.ReservationDomainServiceTest#conflictScenarios")
        @DisplayName("AC-02: 冲突检测表驱动测试（场景1-7）")
        void shouldDetectConflict_perAc02_scenarios1to7(ConflictScenario scenario) {
            // Given: 模拟仓储返回已有预约时间段
            ReservationRepository repository = mock(ReservationRepository.class);
            when(repository.findActiveTimeSlotsByRoom(eq(1L), eq(scenario.newSlot()), isNull()))
                    .thenReturn(List.of(scenario.existingSlot()));

            ReservationDomainService service = new ReservationDomainService(repository);

            // When: 调用冲突检测
            Result<Void> result = service.checkConflict(1L, scenario.newSlot(), null);

            // Then: 验证冲突检测结果
            if (scenario.hasConflict()) {
                assertThat(result.isFailure())
                        .as("场景%d[%s] 应检测到冲突", scenario.scenarioNumber(), scenario.description())
                        .isTrue();
                assertThat(result.getCode())
                        .as("场景%d[%s] 错误码应为 RESERVATION_CONFLICT", scenario.scenarioNumber(), scenario.description())
                        .isEqualTo("RESERVATION_CONFLICT");
            } else {
                assertThat(result.isSuccess())
                        .as("场景%d[%s] 应无冲突", scenario.scenarioNumber(), scenario.description())
                        .isTrue();
            }
        }

        @Test
        @DisplayName("AC-02: 场景8 — 无已有预约时无冲突")
        void shouldNotDetectConflict_whenNoExistingReservations() {
            // Implements: INV-01, AC-02; plan.md §5.2 核心逻辑 1 场景8
            // Given: 模拟仓储返回空列表（无已有预约）
            LocalDateTime base = LocalDateTime.of(2026, 6, 18, 0, 0);
            TimeSlot newSlot = new TimeSlot(base.withHour(10), base.withHour(11));

            ReservationRepository repository = mock(ReservationRepository.class);
            when(repository.findActiveTimeSlotsByRoom(eq(1L), eq(newSlot), isNull()))
                    .thenReturn(Collections.emptyList());

            ReservationDomainService service = new ReservationDomainService(repository);

            // When: 调用冲突检测
            Result<Void> result = service.checkConflict(1L, newSlot, null);

            // Then: 无冲突
            assertThat(result.isSuccess())
                    .as("场景8: 无已有预约时应无冲突")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("INV-01 冲突检测 — 排除自身预约")
    class ExcludeSelfTests {

        @Test
        @DisplayName("AC-02: 修改预约时排除自身 — 不与自身冲突")
        void shouldNotConflictWithSelf_whenExcluded() {
            // Implements: INV-01, AC-02; plan.md §2.4 关键抽象
            // Given: 修改预约时排除自身 ID
            LocalDateTime base = LocalDateTime.of(2026, 6, 18, 0, 0);
            TimeSlot newSlot = new TimeSlot(base.withHour(10), base.withHour(11));
            Long selfReservationId = 100L;

            ReservationRepository repository = mock(ReservationRepository.class);
            when(repository.findActiveTimeSlotsByRoom(eq(1L), eq(newSlot), eq(selfReservationId)))
                    .thenReturn(Collections.emptyList());

            ReservationDomainService service = new ReservationDomainService(repository);

            // When: 调用冲突检测，排除自身
            Result<Void> result = service.checkConflict(1L, newSlot, selfReservationId);

            // Then: 无冲突（自身已被排除）
            assertThat(result.isSuccess())
                    .as("修改预约时排除自身，应无冲突")
                    .isTrue();

            // 验证仓储调用传入了 excludeReservationId
            verify(repository).findActiveTimeSlotsByRoom(1L, newSlot, selfReservationId);
        }
    }

    @Nested
    @DisplayName("INV-01 冲突检测 — 多条已有预约")
    class MultipleExistingReservationsTests {

        @Test
        @DisplayName("AC-02: 与多条已有预约中的一条冲突 → 检测到冲突")
        void shouldDetectConflict_whenOverlapsWithOneOfMultiple() {
            // Implements: INV-01, AC-02
            // Given: 同一会议室有多条已有预约
            LocalDateTime base = LocalDateTime.of(2026, 6, 18, 0, 0);
            TimeSlot existing1 = new TimeSlot(base.withHour(8), base.withHour(9));    // 08:00-09:00
            TimeSlot existing2 = new TimeSlot(base.withHour(10), base.withHour(11));  // 10:00-11:00
            TimeSlot existing3 = new TimeSlot(base.withHour(14), base.withHour(15));  // 14:00-15:00
            TimeSlot newSlot = new TimeSlot(base.withHour(10).withMinute(30), base.withHour(11).withMinute(30));

            ReservationRepository repository = mock(ReservationRepository.class);
            when(repository.findActiveTimeSlotsByRoom(eq(1L), eq(newSlot), isNull()))
                    .thenReturn(List.of(existing1, existing2, existing3));

            ReservationDomainService service = new ReservationDomainService(repository);

            // When: 调用冲突检测
            Result<Void> result = service.checkConflict(1L, newSlot, null);

            // Then: 检测到与 existing2 的冲突
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCode()).isEqualTo("RESERVATION_CONFLICT");
        }

        @Test
        @DisplayName("AC-02: 与多条已有预约均不冲突 → 无冲突")
        void shouldNotDetectConflict_whenNoOverlapWithAny() {
            // Implements: INV-01, AC-02
            // Given: 同一会议室有多条已有预约，新预约与它们均不重叠
            LocalDateTime base = LocalDateTime.of(2026, 6, 18, 0, 0);
            TimeSlot existing1 = new TimeSlot(base.withHour(8), base.withHour(9));    // 08:00-09:00
            TimeSlot existing2 = new TimeSlot(base.withHour(10), base.withHour(11));  // 10:00-11:00
            TimeSlot newSlot = new TimeSlot(base.withHour(11), base.withHour(12));    // 11:00-12:00（端点相邻不冲突）

            ReservationRepository repository = mock(ReservationRepository.class);
            when(repository.findActiveTimeSlotsByRoom(eq(1L), eq(newSlot), isNull()))
                    .thenReturn(List.of(existing1, existing2));

            ReservationDomainService service = new ReservationDomainService(repository);

            // When: 调用冲突检测
            Result<Void> result = service.checkConflict(1L, newSlot, null);

            // Then: 无冲突
            assertThat(result.isSuccess()).isTrue();
        }
    }
}
