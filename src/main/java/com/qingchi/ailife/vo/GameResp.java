package com.qingchi.ailife.vo;

import lombok.Data;

import java.util.List;

/**
 * 游戏状态响应
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class GameResp {

    private Long sessionId;
    private Integer step;
    private String story;
    private LifeStateVO state;
    private List<ChoiceVO> choices;
    private GameEventVO event;
    private Boolean isEnd;
}
