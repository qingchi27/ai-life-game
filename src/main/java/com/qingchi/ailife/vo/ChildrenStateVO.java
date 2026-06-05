package com.qingchi.ailife.vo;

import lombok.Data;

/**
 * 子女状态视图对象
 *
 * @author hengji-chen
 * @date 2026/6/4
 */
@Data
public class ChildrenStateVO {

    /**
     * 子女数量
     **/
    private Integer count;

    /**
     * 子女能力, 0-100
     **/
    private Integer ability;

    /**
     * 子女成就, 0-100
     **/
    private Integer achievement;
}
