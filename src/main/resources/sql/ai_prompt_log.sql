CREATE TABLE IF NOT EXISTS `ai_prompt_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` BIGINT DEFAULT NULL COMMENT '会话ID',
    `prompt_type` VARCHAR(50) DEFAULT NULL COMMENT '提示类型',
    `prompt_content` TEXT COMMENT '提示内容',
    `response_content` TEXT COMMENT '响应内容',
    `token_usage` INT DEFAULT 0 COMMENT 'Token消耗',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词日志表';
