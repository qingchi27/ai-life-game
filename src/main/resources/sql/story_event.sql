CREATE TABLE IF NOT EXISTS `story_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_code` VARCHAR(50) DEFAULT NULL COMMENT '事件编码',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '事件标题',
    `event_type` VARCHAR(50) DEFAULT NULL COMMENT '事件类型',
    `rarity` INT DEFAULT 1 COMMENT '稀有度',
    `min_age` INT DEFAULT NULL COMMENT '最小年龄',
    `max_age` INT DEFAULT NULL COMMENT '最大年龄',
    `trigger_condition` JSON DEFAULT NULL COMMENT '触发条件',
    `event_content` TEXT COMMENT '事件内容',
    `choices` JSON DEFAULT NULL COMMENT '选项列表',
    `effect` JSON DEFAULT NULL COMMENT '状态影响',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_code` (`event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧情事件池表';
