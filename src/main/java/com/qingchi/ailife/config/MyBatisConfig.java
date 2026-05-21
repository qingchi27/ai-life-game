package com.qingchi.ailife.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Configuration
@MapperScan("com.qingchi.ailife.mapper")
public class MyBatisConfig {
}
