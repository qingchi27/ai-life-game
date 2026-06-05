package com.qingchi.ailife.ai.dto;

import com.qingchi.ailife.domain.LifeState;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 结局摘要 AI 生成上下文
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Data
@Builder
public class EndingContext {

    /**
     * 玩家姓名
     **/
    private String playerName;

    /**
     * 最终年龄
     **/
    private Integer age;

    /**
     * 人生评分
     **/
    private Integer score;

    /**
     * 结局标题
     **/
    private String endingTitle;

    /**
     * 模板结局摘要, 供 AI 扩写
     **/
    private String templateSummary;

    /**
     * 最终状态
     **/
    private LifeState state;

    /**
     * 人生历程摘要
     **/
    private List<String> historySummaries;
}
