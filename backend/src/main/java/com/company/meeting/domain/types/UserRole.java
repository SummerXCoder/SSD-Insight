package com.company.meeting.domain.types;

/**
 * 用户角色枚举 — 系统使用者的角色分类
 * <p>
 * 来源：spec.md#2 术语表(普通用户, 管理员), §3 用户与角色; plan.md#2.1 核心领域概念
 */
public enum UserRole {

    /**
     * 普通用户 — 可发起、修改、取消自己预约的用户
     * Implements: spec.md#3 用户与角色(普通用户)
     */
    REGULAR,

    /**
     * 管理员 — 负责审批预约、管理会议室、管理用户、查看统计的用户
     * Implements: spec.md#3 用户与角色(管理员)
     */
    ADMIN
}
