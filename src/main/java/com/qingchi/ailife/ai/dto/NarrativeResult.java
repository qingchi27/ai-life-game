package com.qingchi.ailife.ai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI 叙事生成结果
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Data
@Builder
public class NarrativeResult {

    /**
     * 剧情文本
     **/
    private String text;

    /**
     * 完整 prompt, 用于日志
     **/
    private String prompt;

    /**
     * token 消耗
     **/
    private int tokenUsage;

    /**
     * 是否来自 DeepSeek
     **/
    private boolean fromAi;
}
