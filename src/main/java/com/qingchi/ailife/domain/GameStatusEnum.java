package com.qingchi.ailife.domain;

import lombok.Getter;

/**
 * 游戏状态枚举
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Getter
public enum GameStatusEnum {

    PLAYING(1, "进行中"),
    ENDED(2, "已结束");

    private final int code;
    private final String name;

    GameStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
