package com.qingchi.ailife.convertor;

import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.engine.LifeStateNormalizer;
import com.qingchi.ailife.entity.GameSession;
import com.qingchi.ailife.entity.GameStep;
import com.qingchi.ailife.util.JsonUtil;
import com.qingchi.ailife.vo.ChildrenStateVO;
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
        vo.setFamilyBackground(state.getFamilyBackground());
        vo.setAffection(state.getAffection());
        vo.setWealth(state.getWealth());
        vo.setPower(state.getPower());
        vo.setFame(state.getFame());
        vo.setHealth(state.getHealth());
        vo.setLifespan(state.getLifespan());
        vo.setChildren(toChildrenVO(state.getChildren()));
        return vo;
    }

    private static ChildrenStateVO toChildrenVO(ChildrenState children) {
        ChildrenStateVO vo = new ChildrenStateVO();
        if (children == null) {
            vo.setCount(0);
            vo.setAbility(0);
            vo.setAchievement(0);
            return vo;
        }
        vo.setCount(children.getCount() == null ? 0 : children.getCount());
        vo.setAbility(children.getAbility() == null ? 0 : children.getAbility());
        vo.setAchievement(children.getAchievement() == null ? 0 : children.getAchievement());
        return vo;
    }

    public static LifeState parseLifeState(String json) {
        return LifeStateNormalizer.parse(json);
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
