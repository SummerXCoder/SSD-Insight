// Implements: plan.md §3.2 异常响应规范, §4.1 防御性策略
// Verifies: T003 完成判据 — 异常响应格式符合 plan.md §3.2 规范
package com.company.meeting.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Nested
    @DisplayName("ErrorResponse 构造与字段")
    class Construction {

        @Test
        @DisplayName("of() 应包含 code、message、timestamp 三个字段")
        void shouldContainCodeMessageTimestamp() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            ErrorResponse response = ErrorResponse.of("RESERVATION_CONFLICT", "会议室A在10:00-11:00已有预约");
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertThat(response.getCode()).isEqualTo("RESERVATION_CONFLICT");
            assertThat(response.getMessage()).isEqualTo("会议室A在10:00-11:00已有预约");
            assertThat(response.getTimestamp()).isBetween(before, after);
        }
    }
}
