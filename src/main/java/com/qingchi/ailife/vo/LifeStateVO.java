package com.qingchi.ailife.vo;

import lombok.Data;

/**
 * 人生状态视图对象
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class LifeStateVO {

    /**
     * 家庭条件
     **/
    private String familyBackground;

    /**
     * 感情, 含亲情友情爱情
     **/
    private Integer affection;

    /**
     * 财富
     **/
    private Integer wealth;

    /**
     * 子女
     **/
    private ChildrenStateVO children;

    /**
     * 权力
     **/
    private Integer power;

    /**
     * 名气
     **/
    private Integer fame;

    /**
     * 身体健康
     **/
    private Integer health;

    /**
     * 预期寿命
     **/
    private Integer lifespan;
}
