package com.company.meeting.domain.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T004: Result 模式单元测试
 * 验证 success/failure 构造与状态判断，符合 plan.md §4.1 Result vs Exception 边界
 * 来源：plan.md#4.1 Result vs Exception 边界, 附录 设计模式
 */
class ResultTest {

    @Nested
    @DisplayName("success 工厂方法")
    class SuccessTests {

        @Test
        @DisplayName("success() 带 data：isSuccess=true, isFailure=false, getData 返回值")
        void successWithData_shouldReturnCorrectState() {
            Result<String> result = Result.success("会议室A");

            assertTrue(result.isSuccess());
            assertFalse(result.isFailure());
            assertEquals("会议室A", result.getData());
            assertNull(result.getCode());
            assertNull(result.getMessage());
        }

        @Test
        @DisplayName("success() 无 data：isSuccess=true, getData 返回 null")
        void successWithoutData_shouldReturnCorrectState() {
            Result<Void> result = Result.success();

            assertTrue(result.isSuccess());
            assertFalse(result.isFailure());
            assertNull(result.getData());
            assertNull(result.getCode());
            assertNull(result.getMessage());
        }
    }

    @Nested
    @DisplayName("failure 工厂方法")
    class FailureTests {

        @Test
        @DisplayName("failure(code, message)：isFailure=true, isSuccess=false, getCode/getMessage 返回值")
        void failure_shouldReturnCorrectState() {
            Result<Void> result = Result.failure("RESERVATION_CONFLICT", "会议室A在10:00-11:00已有预约");

            assertTrue(result.isFailure());
            assertFalse(result.isSuccess());
            assertEquals("RESERVATION_CONFLICT", result.getCode());
            assertEquals("会议室A在10:00-11:00已有预约", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("failure() 带 data：getData 返回 null（failure 不携带 data）")
        void failure_shouldNotCarryData() {
            Result<String> result = Result.failure("TIME_CONSTRAINT_VIOLATION", "预约时间无效");

            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("状态判断与边界")
    class StateTests {

        @Test
        @DisplayName("success 与 failure 互斥：同一 Result 不可能同时 success 和 failure")
        void successAndFailureMutuallyExclusive() {
            Result<String> successResult = Result.success("data");
            Result<Void> failureResult = Result.failure("BANNED", "用户被禁");

            assertNotEquals(successResult.isSuccess(), successResult.isFailure());
            assertNotEquals(failureResult.isSuccess(), failureResult.isFailure());
        }
    }
}
