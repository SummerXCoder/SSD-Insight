package com.company.meeting.domain.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * T008: TimeSlot 值对象单元测试
 * <p>
 * 覆盖 plan.md §5.2 核心逻辑 2（时间约束校验）全部 5 个场景：
 * 1. 提前量≥1h → 合法
 * 2. 提前量<1h → 非法
 * 3. 时长>2h → 非法
 * 4. 超出14天范围 → 非法
 * 5. 在14天范围内 → 合法
 * <p>
 * 测试命名引用 AC-09，业务不变量引用 INV-05。
 * 来源：spec.md#INV-05, AC-09; plan.md#5.2 核心逻辑 2
 */
class TimeSlotTest {

    /**
     * 表格驱动测试数据：plan.md §5.2 核心逻辑 2
     * <p>
     * | # | 当前时间 | 预约开始时间 | 预约时长 | 是否合法 | 违规原因 |
     * |---|----------|-------------|----------|----------|----------|
     * | 1 | 09:00    | 10:00       | 1h       | 合法     | 提前1h，时长≤2h |
     * | 2 | 09:00    | 09:30       | 1h       | 非法     | 提前量<1h |
     * | 3 | 09:00    | 10:00       | 3h       | 非法     | 时长>2h |
     * | 4 | 09:00    | 第15天10:00 | 1h       | 非法     | 超出14天范围 |
     * | 5 | 09:00    | 第14天08:00 | 1h       | 合法     | 在14天范围内 |
     */
    record TimeConstraintScenario(
            String description,
            LocalDateTime now,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean valid,
            String expectedErrorCode
    ) {}

    static Stream<TimeConstraintScenario> timeConstraintScenarios() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);

        return Stream.of(
                // 场景1: 提前1h，时长≤2h → 合法
                // Implements: INV-05, AC-09
                new TimeConstraintScenario(
                        "提前1h且时长1h → 合法",
                        now,
                        now.plusHours(1),
                        now.plusHours(2),
                        true,
                        null
                ),
                // 场景2: 提前量<1h → 非法
                // Implements: INV-05, AC-09
                new TimeConstraintScenario(
                        "提前量不足1h → 非法",
                        now,
                        now.plusMinutes(30),
                        now.plusHours(1).plusMinutes(30),
                        false,
                        "TIME_CONSTRAINT_VIOLATION"
                ),
                // 场景3: 时长>2h → 非法
                // Implements: INV-05, AC-09
                new TimeConstraintScenario(
                        "时长3h超过2h限制 → 非法",
                        now,
                        now.plusHours(1),
                        now.plusHours(4),
                        false,
                        "TIME_CONSTRAINT_VIOLATION"
                ),
                // 场景4: 超出14天范围 → 非法
                // Implements: INV-05, AC-09
                new TimeConstraintScenario(
                        "预约第15天超出14天范围 → 非法",
                        now,
                        now.plusDays(15).withHour(10).withMinute(0),
                        now.plusDays(15).withHour(11).withMinute(0),
                        false,
                        "TIME_CONSTRAINT_VIOLATION"
                ),
                // 场景5: 在14天范围内 → 合法
                // Implements: INV-05, AC-09
                new TimeConstraintScenario(
                        "预约第14天在14天范围内 → 合法",
                        now,
                        now.plusDays(14).withHour(8).withMinute(0),
                        now.plusDays(14).withHour(9).withMinute(0),
                        true,
                        null
                )
        );
    }

    @Nested
    @DisplayName("INV-05 时间约束校验 — plan.md §5.2 核心逻辑 2")
    class TimeConstraintValidationTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.company.meeting.domain.types.TimeSlotTest#timeConstraintScenarios")
        @DisplayName("AC-09: 时间约束校验表驱动测试")
        void shouldValidateTimeConstraints_perAc09(TimeConstraintScenario scenario) {
            // Given: 构造 TimeSlot
            TimeSlot timeSlot = new TimeSlot(scenario.startTime(), scenario.endTime());

            // When: 调用自校验方法
            Result<Void> result = timeSlot.validateConstraints(scenario.now());

            // Then: 验证结果
            if (scenario.valid()) {
                assertThat(result.isSuccess())
                        .as("场景[%s] 应为合法: 提前量≥1h, 时长≤2h, 范围≤14天", scenario.description())
                        .isTrue();
            } else {
                assertThat(result.isFailure())
                        .as("场景[%s] 应为非法", scenario.description())
                        .isTrue();
                assertThat(result.getCode())
                        .as("场景[%s] 错误码应为 TIME_CONSTRAINT_VIOLATION", scenario.description())
                        .isEqualTo(scenario.expectedErrorCode());
            }
        }
    }

    @Nested
    @DisplayName("INV-05 边界值补充测试")
    class BoundaryTests {

        @Test
        @DisplayName("AC-09: 提前恰好1h → 合法（边界值）")
        void shouldAllow_whenAdvanceExactly1Hour() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            TimeSlot timeSlot = new TimeSlot(now.plusHours(1), now.plusHours(2));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("AC-09: 提前59分钟 → 非法（边界值）")
        void shouldReject_whenAdvance59Minutes() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            TimeSlot timeSlot = new TimeSlot(now.plusMinutes(59), now.plusHours(1).plusMinutes(59));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCode()).isEqualTo("TIME_CONSTRAINT_VIOLATION");
        }

        @Test
        @DisplayName("AC-09: 时长恰好2h → 合法（边界值）")
        void shouldAllow_whenDurationExactly2Hours() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            TimeSlot timeSlot = new TimeSlot(now.plusHours(1), now.plusHours(3));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("AC-09: 时长2h1min → 非法（边界值）")
        void shouldReject_whenDuration2Hours1Minute() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            TimeSlot timeSlot = new TimeSlot(now.plusHours(1), now.plusHours(3).plusMinutes(1));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCode()).isEqualTo("TIME_CONSTRAINT_VIOLATION");
        }

        @Test
        @DisplayName("AC-09: 恰好14天范围 → 合法（边界值）")
        void shouldAllow_whenExactly14DaysAhead() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            // 第14天的同一时刻，即 now + 14天 = 7月1日 09:00
            // 开始时间 = now + 14天，仍在14天范围内（≤14天）
            TimeSlot timeSlot = new TimeSlot(now.plusDays(14), now.plusDays(14).plusHours(1));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("AC-09: 超出14天1分钟 → 非法（边界值）")
        void shouldReject_when14DaysPlus1Minute() {
            // Implements: INV-05, AC-09
            LocalDateTime now = LocalDateTime.of(2026, 6, 17, 9, 0);
            TimeSlot timeSlot = new TimeSlot(now.plusDays(14).plusMinutes(1), now.plusDays(14).plusHours(1).plusMinutes(1));

            Result<Void> result = timeSlot.validateConstraints(now);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCode()).isEqualTo("TIME_CONSTRAINT_VIOLATION");
        }
    }

    @Nested
    @DisplayName("TimeSlot 构造校验")
    class ConstructionTests {

        @Test
        @DisplayName("startTime 必须早于 endTime")
        void shouldReject_whenStartTimeEqualsEndTime() {
            LocalDateTime time = LocalDateTime.of(2026, 6, 17, 10, 0);

            assertThatThrownBy(() -> new TimeSlot(time, time))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("startTime");
        }

        @Test
        @DisplayName("startTime 晚于 endTime 应拒绝")
        void shouldReject_whenStartTimeAfterEndTime() {
            LocalDateTime start = LocalDateTime.of(2026, 6, 17, 11, 0);
            LocalDateTime end = LocalDateTime.of(2026, 6, 17, 10, 0);

            assertThatThrownBy(() -> new TimeSlot(start, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("startTime");
        }
    }
}
