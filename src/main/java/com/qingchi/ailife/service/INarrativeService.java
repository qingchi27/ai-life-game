package com.qingchi.ailife.service;

import com.qingchi.ailife.ai.dto.EndingContext;
import com.qingchi.ailife.ai.dto.NarrativeContext;
import com.qingchi.ailife.ai.dto.NarrativeResult;

/**
 * AI 叙事服务, DeepSeek 只负责文本生成
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
public interface INarrativeService {

    /**
     * 根据上下文生成剧情, 失败时回退模板
     *
     * @param {NarrativeContext} context - 叙事上下文
     * @returns {NarrativeResult} 生成结果
     */
    NarrativeResult generate(NarrativeContext context);

    /**
     * 生成结局摘要, 失败时回退模板
     *
     * @param {EndingContext} context - 结局上下文
     * @returns {NarrativeResult} 生成结果
     */
    NarrativeResult generateEnding(EndingContext context);
}
