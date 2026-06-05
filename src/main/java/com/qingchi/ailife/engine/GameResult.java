package com.qingchi.ailife.engine;

import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.vo.ChoiceVO;
import com.qingchi.ailife.vo.GameEventVO;
import lombok.Data;

import java.util.List;

/**
 * 游戏引擎单步结果
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class GameResult {

    private String story;
    private LifeState state;
    private List<ChoiceVO> choices;
    private GameEventVO event;
    private String eventType;
    private boolean useAi;
    private boolean end;
    private int ageDelta;

    /**
     * AI 请求 prompt, 用于日志
     **/
    private String aiPrompt;

    /**
     * AI token 消耗
     **/
    private Integer tokenUsage;
}
