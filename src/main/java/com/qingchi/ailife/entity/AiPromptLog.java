package com.qingchi.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI提示词日志实体
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class AiPromptLog {

    /**
     * 主键
     **/
    private Long id;

    /**
     * 会话ID
     **/
    private Long sessionId;

    /**
     * 提示类型
     **/
    private String promptType;

    /**
     * 提示内容
     **/
    private String promptContent;

    /**
     * 响应内容
     **/
    private String responseContent;

    /**
     * Token消耗
     **/
    private Integer tokenUsage;

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
