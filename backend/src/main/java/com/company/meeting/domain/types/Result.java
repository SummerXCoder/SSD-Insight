package com.company.meeting.domain.types;

/**
 * 领域层 Result 模式 — 区分业务规则冲突与系统故障
 * <p>
 * 可预期的业务失败用 Result，不可预期的系统故障用 Exception。
 * 领域服务返回 Result&lt;T&gt;，Application 层根据 isFailure() 决定抛 BusinessException。
 * <p>
 * 来源：plan.md#4.1 Result vs Exception 边界, 附录 设计模式
 */
public class Result<T> {

    private final boolean success;
    private final T data;
    private final String code;
    private final String message;

    private Result(boolean success, T data, String code, String message) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }

    public static Result<Void> success() {
        return new Result<>(true, null, null, null);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, null, code, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public T getData() {
        return data;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
