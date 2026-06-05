package com.qingchi.ailife.ai.client;

import com.qingchi.ailife.ai.parser.DeepSeekResponseParser;
import com.qingchi.ailife.config.DeepSeekProperties;
import com.qingchi.ailife.util.JsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek Chat Completions 客户端
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Slf4j
@Component
public class DeepSeekClient {

    @Resource
    private RestClient deepSeekRestClient;

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private DeepSeekResponseParser responseParser;

    /**
     * 调用 DeepSeek 生成文本
     *
     * @param {String} systemPrompt - 系统提示词
     * @param {String} userPrompt - 用户提示词
     * @returns {DeepSeekResponseParser.ParsedResponse} 解析结果
     */
    public DeepSeekResponseParser.ParsedResponse chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", deepSeekProperties.getModel());
        body.put("max_tokens", deepSeekProperties.getMaxTokens());
        body.put("temperature", 0.85);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        log.info("调用 DeepSeek 生成剧情, model: {}", deepSeekProperties.getModel());
        try {
            String responseBody = deepSeekRestClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(JsonUtil.toJson(body))
                    .retrieve()
                    .body(String.class);
            DeepSeekResponseParser.ParsedResponse parsed = responseParser.parse(responseBody);
            log.info("DeepSeek 剧情生成成功, tokenUsage: {}", parsed.tokenUsage());
            return parsed;
        } catch (RestClientException ex) {
            log.warn("DeepSeek 调用失败, 将使用模板文案, reason: {}", ex.getMessage());
            return DeepSeekResponseParser.ParsedResponse.empty();
        }
    }
}
