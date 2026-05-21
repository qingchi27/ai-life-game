package com.qingchi.ailife.vo;

import lombok.Data;

import java.util.List;

/**
 * 结束游戏响应
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class EndGameResp {

    private String endingTitle;
    private String summary;
    private Integer score;
    private List<String> tags;
}
