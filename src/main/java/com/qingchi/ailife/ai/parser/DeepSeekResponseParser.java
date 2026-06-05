package com.qingchi.ailife.ai.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.qingchi.ailife.util.JsonUtil;
import org.springframework.stereotype.Component;

/**
 * DeepSeek Chat Completions 响应解析
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Component
public class DeepSeekResponseParser {

    /**
     * 解析 Chat Completions 响应
     *
     * @param {String} responseBody - 原始 JSON
     * @returns {ParsedResponse} 文本与 token 用量
     */
    public ParsedResponse parse(String responseBody) {
        JsonNode root = JsonUtil.readTree(responseBody);
        if (root == null) {
            return ParsedResponse.empty();
        }
        JsonNode choices = root.get("choices");
        String text = "";
        if (choices != null && choices.isArray() && !choices.isEmpty()) {
            JsonNode message = choices.get(0).get("message");
            if (message != null && message.has("content")) {
                text = message.get("content").asText("").trim();
            }
        }
        int tokens = 0;
        JsonNode usage = root.get("usage");
        if (usage != null && usage.has("total_tokens")) {
            tokens = usage.get("total_tokens").asInt(0);
        }
        return new ParsedResponse(text, tokens);
    }

    /**
     * 解析结果
     */
    public record ParsedResponse(String text, int tokenUsage) {

        public static ParsedResponse empty() {
            return new ParsedResponse("", 0);
        }
    }
}
