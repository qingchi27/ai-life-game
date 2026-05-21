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

    private Integer money;
    private Integer health;
    private Integer luck;
    private String career;
    private Integer relationship;
}
