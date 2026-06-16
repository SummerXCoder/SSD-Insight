// Implements: plan.md §4.1 防御性策略 — BusinessException 映射 HTTP 400
package com.company.meeting.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Nested
    @DisplayName("BusinessException 构造与字段")
    class Construction {

        @Test
        @DisplayName("应正确保存 code 和 message")
        void shouldContainCodeAndMessage() {
            BusinessException ex = new BusinessException("TIME_CONSTRAINT_VIOLATION", "预约时间不满足约束");

            assertThat(ex.getCode()).isEqualTo("TIME_CONSTRAINT_VIOLATION");
            assertThat(ex.getMessage()).isEqualTo("预约时间不满足约束");
        }

        @Test
        @DisplayName("应继承 RuntimeException")
        void shouldBeRuntimeException() {
            BusinessException ex = new BusinessException("TEST_CODE", "test message");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
