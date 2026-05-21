CREATE TABLE IF NOT EXISTS `game_step` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `step_no` INT NOT NULL COMMENT '步数序号',
    `story` TEXT COMMENT '剧情内容',
    `user_choice` VARCHAR(255) DEFAULT NULL COMMENT '用户选择',
    `state_before` JSON DEFAULT NULL COMMENT '选择前状态',
    `state_after` JSON DEFAULT NULL COMMENT '选择后状态',
    `event_type` VARCHAR(50) DEFAULT NULL COMMENT '事件类型',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏步骤表';
