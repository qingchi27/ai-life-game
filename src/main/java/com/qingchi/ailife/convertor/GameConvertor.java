package com.qingchi.ailife.convertor;

import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.entity.GameSession;
import com.qingchi.ailife.entity.GameStep;
import com.qingchi.ailife.util.JsonUtil;
import com.qingchi.ailife.vo.ChoiceVO;
import com.qingchi.ailife.vo.GameResp;
import com.qingchi.ailife.vo.HistoryStepVO;
import com.qingchi.ailife.vo.LifeStateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

/**
 * 游戏对象转换器
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public final class GameConvertor {

    private GameConvertor() {
    }

    public static LifeStateVO toLifeStateVO(LifeState state) {
        if (state == null) {
            return null;
        }
        LifeStateVO vo = new LifeStateVO();
        vo.setMoney(state.getMoney());
        vo.setHealth(state.getHealth());
        vo.setLuck(state.getLuck());
        vo.setCareer(state.getCareer());
        vo.setRelationship(state.getRelationship());
        return vo;
    }

    public static LifeState parseLifeState(String json) {
        return JsonUtil.fromJson(json, LifeState.class);
    }

    public static List<ChoiceVO> parseChoices(String json) {
        return JsonUtil.fromJson(json, new TypeReference<>() {});
    }

    public static GameResp toGameResp(GameSession session) {
        GameResp resp = new GameResp();
        resp.setSessionId(session.getId());
        resp.setStep(session.getCurrentStep());
        resp.setStory(session.getCurrentStory());
        resp.setState(toLifeStateVO(parseLifeState(session.getLifeStatus())));
        resp.setChoices(parseChoices(session.getCurrentChoices()));
        resp.setIsEnd(session.getGameStatus() != null && session.getGameStatus() == 2);
        return resp;
    }

    public static HistoryStepVO toHistoryStepVO(GameStep step) {
        HistoryStepVO vo = new HistoryStepVO();
        vo.setStep(step.getStepNo());
        vo.setStory(step.getStory());
        vo.setChoice(step.getUserChoice());
        return vo;
    }
}
