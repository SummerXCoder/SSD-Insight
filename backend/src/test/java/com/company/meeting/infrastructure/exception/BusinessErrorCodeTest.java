// Implements: T005 — BusinessErrorCode 枚举单元测试
// 验证：枚举值覆盖 plan.md §3.2 与 §4.1 中所有业务错误场景
// 引用：spec.md INV-01/INV-04/INV-05/INV-07, AC-02/AC-09/AC-10/AC-15/AC-18
package com.company.meeting.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessErrorCodeTest {

    @Nested
    @DisplayName("plan.md §3.2/§4.1 规定的业务错误码必须存在")
    class AllErrorCodesDefined {

        @Test
        @DisplayName("RESERVATION_CONFLICT — INV-01 同室同时段冲突 (AC-02)")
        void reservationConflict_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.RESERVATION_CONFLICT;
            assertThat(errorCode.getCode()).isEqualTo("RESERVATION_CONFLICT");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("TIME_CONSTRAINT_VIOLATION — INV-05 时间约束违规 (AC-09)")
        void timeConstraintViolation_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.TIME_CONSTRAINT_VIOLATION;
            assertThat(errorCode.getCode()).isEqualTo("TIME_CONSTRAINT_VIOLATION");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("RESERVATION_BANNED — INV-07 禁令期间预约 (AC-18)")
        void reservationBanned_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.RESERVATION_BANNED;
            assertThat(errorCode.getCode()).isEqualTo("RESERVATION_BANNED");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("ROOM_DISABLED — INV-04 停用室预约 (AC-10)")
        void roomDisabled_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.ROOM_DISABLED;
            assertThat(errorCode.getCode()).isEqualTo("ROOM_DISABLED");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("CHECK_IN_NOT_AVAILABLE — 签到窗口外 (AC-15)")
        void checkInNotAvailable_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.CHECK_IN_NOT_AVAILABLE;
            assertThat(errorCode.getCode()).isEqualTo("CHECK_IN_NOT_AVAILABLE");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("DUPLICATE_USERNAME — 用户名重复 (SCN-08)")
        void duplicateUsername_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.DUPLICATE_USERNAME;
            assertThat(errorCode.getCode()).isEqualTo("DUPLICATE_USERNAME");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("UNAUTHORIZED — 未认证 (AC-12)")
        void unauthorized_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.UNAUTHORIZED;
            assertThat(errorCode.getCode()).isEqualTo("UNAUTHORIZED");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("FORBIDDEN — 无权限")
        void forbidden_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.FORBIDDEN;
            assertThat(errorCode.getCode()).isEqualTo("FORBIDDEN");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("RESOURCE_NOT_FOUND — 资源不存在")
        void resourceNotFound_mustExist() {
            BusinessErrorCode errorCode = BusinessErrorCode.RESOURCE_NOT_FOUND;
            assertThat(errorCode.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
            assertThat(errorCode.getDefaultMessage()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("枚举结构约束")
    class EnumStructureConstraints {

        @Test
        @DisplayName("每个枚举值的 code 必须唯一")
        void code_mustBeUnique() {
            Set<String> codes = Arrays.stream(BusinessErrorCode.values())
                    .map(BusinessErrorCode::getCode)
                    .collect(Collectors.toSet());
            assertThat(codes).hasSize(BusinessErrorCode.values().length);
        }

        @Test
        @DisplayName("每个枚举值的 defaultMessage 必须非空")
        void defaultMessage_mustBeNotBlank() {
            for (BusinessErrorCode errorCode : BusinessErrorCode.values()) {
                assertThat(errorCode.getDefaultMessage())
                        .as("BusinessErrorCode.%s 的 defaultMessage 不能为空", errorCode.name())
                        .isNotBlank();
            }
        }

        @Test
        @DisplayName("枚举值数量必须覆盖 tasks.md T005 规定的 9 个错误码")
        void enumCount_mustCoverAllRequired() {
            assertThat(BusinessErrorCode.values()).hasSize(9);
        }
    }

    @Nested
    @DisplayName("与 BusinessException 集成")
    class BusinessExceptionIntegration {

        @Test
        @DisplayName("BusinessErrorCode 可直接构造 BusinessException")
        void should_createBusinessException_fromErrorCode() {
            BusinessErrorCode errorCode = BusinessErrorCode.RESERVATION_CONFLICT;
            BusinessException exception = new BusinessException(
                    errorCode.getCode(), errorCode.getDefaultMessage());

            assertThat(exception.getCode()).isEqualTo("RESERVATION_CONFLICT");
            assertThat(exception.getMessage()).isNotBlank();
        }
    }
}
