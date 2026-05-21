package com.qingchi.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏会话实体
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class GameSession {

    /**
     * 主键
     **/
    private Long id;

    /**
     * 用户ID
     **/
    private Long userId;

    /**
     * 会话编号
     **/
    private String sessionNo;

    /**
     * 玩家姓名
     **/
    private String playerName;

    /**
     * 当前年龄
     **/
    private Integer currentAge;

    /**
     * 当前步数
     **/
    private Integer currentStep;

    /**
     * 人生状态JSON
     **/
    private String lifeStatus;

    /**
     * 当前剧情
     **/
    private String currentStory;

    /**
     * 当前选项JSON
     **/
    private String currentChoices;

    /**
     * 游戏状态
     **/
    private Integer gameStatus;

    /**
     * 结局摘要
     **/
    private String endingSummary;

    /**
     * 人生评分
     **/
    private Integer score;

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
