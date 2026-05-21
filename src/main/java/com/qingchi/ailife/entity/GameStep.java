package com.qingchi.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏步骤实体
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class GameStep {

    /**
     * 主键
     **/
    private Long id;

    /**
     * 会话ID
     **/
    private Long sessionId;

    /**
     * 步数序号
     **/
    private Integer stepNo;

    /**
     * 剧情内容
     **/
    private String story;

    /**
     * 用户选择
     **/
    private String userChoice;

    /**
     * 选择前状态JSON
     **/
    private String stateBefore;

    /**
     * 选择后状态JSON
     **/
    private String stateAfter;

    /**
     * 事件类型
     **/
    private String eventType;

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
