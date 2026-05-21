package com.qingchi.ailife.domain;

import lombok.Data;

/**
 * 人生状态核心对象
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class LifeState {

    /**
     * 金钱
     **/
    private Integer money;

    /**
     * 健康
     **/
    private Integer health;

    /**
     * 运气
     **/
    private Integer luck;

    /**
     * 职业
     **/
    private String career;

    /**
     * 人际关系
     **/
    private Integer relationship;
}
