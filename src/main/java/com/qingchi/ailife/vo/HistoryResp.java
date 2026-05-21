package com.qingchi.ailife.vo;

import lombok.Data;

import java.util.List;

/**
 * 人生轨迹响应
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class HistoryResp {

    private List<HistoryStepVO> steps;
}
