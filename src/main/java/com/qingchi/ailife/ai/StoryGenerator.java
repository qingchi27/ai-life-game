package com.qingchi.ailife.ai;

import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.engine.GameResult;
import com.qingchi.ailife.vo.ChoiceVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 剧情生成器（MVP阶段本地模板, 后续可接入大模型）
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Component
public class StoryGenerator {

    /**
     * 生成开局剧情
     *
     * @param {String} playerName - 玩家姓名
     * @returns {GameResult} 开局剧情
     */
    public GameResult generateOpening(String playerName) {
        GameResult result = new GameResult();
        LifeState state = defaultState();
        result.setState(state);
        result.setStory(String.format("%s, 你出生在一个普通家庭, 18岁那年你选择了程序员这条路...", playerName));
        result.setChoices(List.of(
                new ChoiceVO("A", "努力工作"),
                new ChoiceVO("B", "开始副业"),
                new ChoiceVO("C", "躺平")
        ));
        result.setUseAi(true);
        result.setAgeDelta(0);
        result.setEnd(false);
        return result;
    }

    /**
     * 生成关键节点AI剧情
     *
     * @param {LifeState} state - 当前状态
     * @param {String} choiceContent - 用户选择
     * @param {int} step - 当前步数
     * @returns {GameResult} AI剧情
     */
    public GameResult generateKeyStory(LifeState state, String choiceContent, int step) {
        GameResult result = new GameResult();
        result.setState(copyState(state));
        String career = state.getCareer() == null ? "打工人" : state.getCareer();
        result.setStory(String.format("第%d年, 你选择了「%s」, 职业方向逐渐向%s靠拢, 人生出现新的转折...", step, choiceContent, career));
        result.setChoices(List.of(
                new ChoiceVO("A", "扩大副业"),
                new ChoiceVO("B", "继续稳定上班"),
                new ChoiceVO("C", "辞职创业")
        ));
        result.setUseAi(true);
        result.setAgeDelta(2);
        result.setEnd(false);
        return result;
    }

    /**
     * 生成结局文案
     *
     * @param {LifeState} state - 最终状态
     * @param {String} playerName - 玩家姓名
     * @returns {String} 结局摘要
     */
    public String generateEndingSummary(LifeState state, String playerName) {
        return String.format("%s的一生落下帷幕: 积蓄约%d元, 健康%d, 最终职业是%s。",
                playerName, state.getMoney(), state.getHealth(), state.getCareer());
    }

    private LifeState defaultState() {
        LifeState state = new LifeState();
        state.setMoney(3000);
        state.setHealth(80);
        state.setLuck(50);
        state.setCareer("程序员");
        state.setRelationship(40);
        return state;
    }

    private LifeState copyState(LifeState source) {
        LifeState target = new LifeState();
        target.setMoney(source.getMoney());
        target.setHealth(source.getHealth());
        target.setLuck(source.getLuck());
        target.setCareer(source.getCareer());
        target.setRelationship(source.getRelationship());
        return target;
    }
}
