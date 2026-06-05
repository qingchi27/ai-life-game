package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.ai.client.DeepSeekClient;
import com.qingchi.ailife.ai.dto.EndingContext;
import com.qingchi.ailife.ai.dto.NarrativeContext;
import com.qingchi.ailife.ai.dto.NarrativeResult;
import com.qingchi.ailife.ai.parser.DeepSeekResponseParser;
import com.qingchi.ailife.ai.prompt.NarrativePromptBuilder;
import com.qingchi.ailife.config.DeepSeekProperties;
import com.qingchi.ailife.service.INarrativeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 叙事服务实现
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Slf4j
@Service
public class NarrativeServiceImpl implements INarrativeService {

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private NarrativePromptBuilder promptBuilder;

    @Resource
    private DeepSeekClient deepSeekClient;

    @Override
    public NarrativeResult generate(NarrativeContext context) {
        String template = context.getEventTemplate();
        if (template == null || template.isBlank()) {
            template = "你继续自己的人生旅程。";
        }

        if (!Boolean.TRUE.equals(deepSeekProperties.getEnabled())) {
            log.info("DeepSeek 未启用, 使用模板剧情");
            return NarrativeResult.builder()
                    .text(template)
                    .prompt("")
                    .tokenUsage(0)
                    .fromAi(false)
                    .build();
        }
        if (deepSeekProperties.getApiKey() == null || deepSeekProperties.getApiKey().isBlank()) {
            log.warn("DeepSeek API Key 未配置, 使用模板剧情");
            return fallback(template);
        }

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(context);
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        DeepSeekResponseParser.ParsedResponse response =
                deepSeekClient.chat(systemPrompt, userPrompt);

        if (response.text() == null || response.text().isBlank()) {
            log.warn("DeepSeek 返回空文本, 使用模板剧情");
            return fallback(template, fullPrompt);
        }

        return NarrativeResult.builder()
                .text(response.text())
                .prompt(fullPrompt)
                .tokenUsage(response.tokenUsage())
                .fromAi(true)
                .build();
    }

    @Override
    public NarrativeResult generateEnding(EndingContext context) {
        String template = context.getTemplateSummary();
        if (template == null || template.isBlank()) {
            template = context.getPlayerName() + "的一生落下帷幕。";
        }

        if (!Boolean.TRUE.equals(deepSeekProperties.getEnabled())) {
            log.info("DeepSeek 未启用, 使用模板结局");
            return fallback(template);
        }
        if (deepSeekProperties.getApiKey() == null || deepSeekProperties.getApiKey().isBlank()) {
            log.warn("DeepSeek API Key 未配置, 使用模板结局");
            return fallback(template);
        }

        String systemPrompt = promptBuilder.buildEndingSystemPrompt();
        String userPrompt = promptBuilder.buildEndingUserPrompt(context);
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        DeepSeekResponseParser.ParsedResponse response =
                deepSeekClient.chat(systemPrompt, userPrompt);

        if (response.text() == null || response.text().isBlank()) {
            log.warn("DeepSeek 结局返回空文本, 使用模板结局");
            return fallback(template, fullPrompt);
        }

        return NarrativeResult.builder()
                .text(response.text())
                .prompt(fullPrompt)
                .tokenUsage(response.tokenUsage())
                .fromAi(true)
                .build();
    }

    private NarrativeResult fallback(String template) {
        return fallback(template, "");
    }

    private NarrativeResult fallback(String template, String prompt) {
        return NarrativeResult.builder()
                .text(template)
                .prompt(prompt)
                .tokenUsage(0)
                .fromAi(false)
                .build();
    }
}
