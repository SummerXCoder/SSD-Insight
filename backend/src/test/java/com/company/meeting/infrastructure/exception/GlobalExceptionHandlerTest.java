// Implements: plan.md §4.1 防御性策略 — BusinessException 映射 HTTP 400
// Verifies: T003 完成判据 — 手动抛出 BusinessException，确认返回 400 + 正确 JSON 格式
package com.company.meeting.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/business-error")
        public String throwBusinessError() {
            throw new BusinessException("RESERVATION_CONFLICT", "会议室A在10:00-11:00已有预约，存在时间冲突");
        }

        @GetMapping("/test/system-error")
        public String throwSystemError() {
            throw new RuntimeException("数据库连接失败");
        }
    }

    @Nested
    @DisplayName("BusinessException 处理")
    class BusinessExceptionHandling {

        @Test
        @DisplayName("抛出 BusinessException 应返回 400 + {code, message, timestamp}")
        void shouldReturn400WithErrorResponse() throws Exception {
            mockMvc.perform(get("/test/business-error"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"))
                    .andExpect(jsonPath("$.message").value("会议室A在10:00-11:00已有预约，存在时间冲突"))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("系统异常处理")
    class SystemExceptionHandling {

        @Test
        @DisplayName("未处理异常应返回 500 + {code: INTERNAL_ERROR, message, timestamp}")
        void shouldReturn500WithInternalError() throws Exception {
            mockMvc.perform(get("/test/system-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.message").value("系统内部错误"))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }
}
