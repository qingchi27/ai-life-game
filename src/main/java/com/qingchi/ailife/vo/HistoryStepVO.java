package com.qingchi.ailife.vo;

import lombok.Data;

/**
 * 人生轨迹单步视图
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class HistoryStepVO {

    private Integer step;
    private String story;
    private String choice;
}
