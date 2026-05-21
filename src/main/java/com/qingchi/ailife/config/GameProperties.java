package com.qingchi.ailife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 游戏配置属性
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
@Component
@ConfigurationProperties(prefix = "game")
public class GameProperties {

    /**
     * 最大步数
     **/
    private Integer maxStep = 30;

    /**
     * 默认用户ID
     **/
    private Long defaultUserId = 1L;
}
