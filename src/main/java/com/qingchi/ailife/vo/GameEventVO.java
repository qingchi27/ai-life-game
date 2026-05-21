package com.qingchi.ailife.vo;

import lombok.Data;

/**
 * 游戏事件视图对象
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class GameEventVO {

    /**
     * 事件类型
     **/
    private String type;

    /**
     * 事件标题
     **/
    private String title;
}
