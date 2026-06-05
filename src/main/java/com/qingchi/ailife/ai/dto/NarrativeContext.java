package com.qingchi.ailife.ai.dto;

import com.qingchi.ailife.domain.LifeState;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI 叙事生成上下文
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Data
@Builder
public class NarrativeContext {

    /**
     * 玩家姓名
     **/
    private String playerName;

    /**
     * 当前年龄
     **/
    private Integer age;

    /**
     * 当前步数
     **/
    private Integer step;

    /**
     * 用户本步选择文案
     **/
    private String userChoice;

    /**
     * 触发事件名称
     **/
    private String eventTitle;

    /**
     * 事件类型
     **/
    private String eventType;

    /**
     * 事件模板描述, 供 AI 扩写
     **/
    private String eventTemplate;

    /**
     * 当前人生状态
     **/
    private LifeState state;

    /**
     * 最近历史摘要
     **/
    private List<String> historySummaries;

    /**
     * 是否为开局
     **/
    private boolean opening;
}
