package com.qingchi.ailife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /**
     * 是否启用 AI 叙事
     **/
    private Boolean enabled = false;

    /**
     * API 密钥, 建议通过环境变量 DEEPSEEK_API_KEY 注入
     **/
    private String apiKey = "";

    /**
     * API 基础地址
     **/
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 模型名称
     **/
    private String model = "deepseek-chat";

    /**
     * 单次生成最大 token
     **/
    private Integer maxTokens = 400;

    /**
     * 请求超时秒数
     **/
    private Integer timeoutSeconds = 30;

    /**
     * 带入 prompt 的最近历史条数
     **/
    private Integer historyLimit = 5;
}
