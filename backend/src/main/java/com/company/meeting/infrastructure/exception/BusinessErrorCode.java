// Implements: T005 [ARCH-D1] [Phase 1] — 业务错误码枚举
// 覆盖：plan.md §3.2 异常响应规范, §4.1 防御性策略
// 引用：spec.md INV-01/INV-04/INV-05/INV-07, AC-02/AC-09/AC-10/AC-15/AC-18
package com.company.meeting.infrastructure.exception;

/**
 * 业务错误码枚举，覆盖所有可预期的业务错误场景。
 * <p>
 * 每个枚举项含 code（字符串标识）和 defaultMessage（默认用户提示）。
 * Application 层根据 Result.isFailure() 取得错误码后，可据此构造 BusinessException。
 * <p>
 * 来源：plan.md §3.2 异常响应规范, §4.1 防御性策略
 */
public enum BusinessErrorCode {

    // Implements: spec.md INV-01, AC-02 — 同一会议室同一时间段存在冲突预约
    RESERVATION_CONFLICT("RESERVATION_CONFLICT", "会议室在该时间段已有预约，存在时间冲突"),

    // Implements: spec.md INV-05, AC-09 — 预约时间不满足基本约束（提前量/时长/范围）
    TIME_CONSTRAINT_VIOLATION("TIME_CONSTRAINT_VIOLATION", "预约时间不满足约束条件"),

    // Implements: spec.md INV-07, AC-18 — 用户处于预约禁令期间
    RESERVATION_BANNED("RESERVATION_BANNED", "您当前处于预约禁令期间，无法发起新预约"),

    // Implements: spec.md INV-04, AC-10 — 会议室处于停用状态
    ROOM_DISABLED("ROOM_DISABLED", "该会议室当前已停用，不可预约"),

    // Implements: spec.md AC-15 — 签到不在允许的时间窗口内
    CHECK_IN_NOT_AVAILABLE("CHECK_IN_NOT_AVAILABLE", "当前不在签到时间窗口内，无法签到"),

    // Implements: spec.md SCN-08 — 注册时用户名已存在
    DUPLICATE_USERNAME("DUPLICATE_USERNAME", "用户名已存在"),

    // Implements: spec.md AC-12 — 未认证访问
    UNAUTHORIZED("UNAUTHORIZED", "未登录或登录已过期，请重新登录"),

    // Implements: 无权限访问
    FORBIDDEN("FORBIDDEN", "您没有权限执行此操作"),

    // Implements: 资源不存在
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "请求的资源不存在");

    private final String code;
    private final String defaultMessage;

    BusinessErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
