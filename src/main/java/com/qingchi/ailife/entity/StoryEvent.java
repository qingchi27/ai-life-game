package com.qingchi.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 剧情事件实体
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class StoryEvent {

    /**
     * 主键
     **/
    private Long id;

    /**
     * 事件编码
     **/
    private String eventCode;

    /**
     * 事件标题
     **/
    private String title;

    /**
     * 事件类型
     **/
    private String eventType;

    /**
     * 稀有度
     **/
    private Integer rarity;

    /**
     * 最小年龄
     **/
    private Integer minAge;

    /**
     * 最大年龄
     **/
    private Integer maxAge;

    /**
     * 触发条件JSON
     **/
    private String triggerCondition;

    /**
     * 事件内容
     **/
    private String eventContent;

    /**
     * 选项JSON
     **/
    private String choices;

    /**
     * 影响JSON
     **/
    private String effect;

    /**
     * 创建时间
     **/
    private LocalDateTime createdTime;

    /**
     * 更新时间
     **/
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除
     **/
    private Integer deleted;
}
