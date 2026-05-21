package com.qingchi.ailife.common;

import lombok.Getter;

/**
 * 业务错误码枚举
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(400, "参数错误"),
    SESSION_NOT_FOUND(40401, "游戏会话不存在"),
    SESSION_ENDED(40402, "游戏已结束"),
    CHOICE_INVALID(40403, "无效的选择"),
    GAME_STATUS_ERROR(40404, "游戏状态异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
