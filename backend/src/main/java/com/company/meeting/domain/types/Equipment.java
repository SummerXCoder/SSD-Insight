package com.company.meeting.domain.types;

/**
 * 设备枚举 — 会议室配备的设施类型
 * <p>
 * 来源：spec.md#2 术语表(设备); plan.md#2.1 核心领域概念
 */
public enum Equipment {

    /**
     * 投影仪
     * Implements: spec.md#2 术语表(设备)
     */
    PROJECTOR,

    /**
     * 白板
     * Implements: spec.md#2 术语表(设备)
     */
    WHITEBOARD,

    /**
     * 视频会议系统
     * Implements: spec.md#2 术语表(设备)
     */
    VIDEO_CONF
}
