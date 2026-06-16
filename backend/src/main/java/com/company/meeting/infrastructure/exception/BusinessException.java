// Implements: plan.md §4.1 防御性策略 — 业务规则冲突（可预期）返回显式业务 Result，HTTP 400 + 业务错误码
package com.company.meeting.infrastructure.exception;

/**
 * 业务异常，映射 HTTP 400。
 * Application 层根据 Result.isFailure() 决定抛出此异常。
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
