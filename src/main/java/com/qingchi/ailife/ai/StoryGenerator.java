package com.qingchi.ailife.ai;

import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.FamilyBackgroundType;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.engine.FamilyBackgroundInitializer;
import com.qingchi.ailife.engine.GameResult;
import com.qingchi.ailife.vo.ChoiceVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        LifeState state = new LifeState();
        FamilyBackgroundType background = FamilyBackgroundInitializer.applyRandom(state);
        result.setState(state);
        result.setStory(String.format(
                "%s, 你出生在%s。18岁那年高考结束, 你考入一所普通大学的计算机专业, 开启了大学生活...",
                playerName, background.getLabel()));
        result.setChoices(List.of(
                new ChoiceVO("A", "努力学习"),
                new ChoiceVO("B", "开始副业"),
                new ChoiceVO("C", "躺平")
        ));
        result.setUseAi(true);
        result.setAgeDelta(0);
        result.setEnd(false);
        return result;
    }

    /**
     * 生成关键节点剧情
     *
     * @param {LifeState} state - 当前状态
     * @param {String} choiceContent - 用户选择
     * @param {int} age - 当前年龄
     * @param {int} milestoneStep - 里程碑步数
     * @returns {GameResult} 关键节点剧情
     */
    public GameResult generateKeyStory(LifeState state, String choiceContent, int age, int milestoneStep) {
        GameResult result = new GameResult();
        String occupation = state.getOccupation() == null ? "社会新人" : state.getOccupation();
        String phase = resolvePhaseDesc(state);
        int phaseIndex = (milestoneStep / 5) % 3;
        result.setStory(buildKeyStoryText(age, phase, choiceContent, occupation, milestoneStep, phaseIndex));
        result.setChoices(buildKeyChoices(phaseIndex));
        result.setUseAi(true);
        result.setAgeDelta(2);
        result.setEnd(false);
        return result;
    }

    private String buildKeyStoryText(int age, String phase, String choiceContent,
                                     String occupation, int milestoneStep, int phaseIndex) {
        String theme = switch (phaseIndex) {
            case 0 -> "事业路径";
            case 1 -> "人脉与感情";
            default -> "生活节奏";
        };
        return String.format(
                "第%d个五年节点, %d岁的你%s。上一步选择了「%s」, 在%s方向上迎来%s的抉择, 人生出现新的转折...",
                milestoneStep / 5, age, phase, choiceContent, occupation, theme);
    }

    private List<ChoiceVO> buildKeyChoices(int phaseIndex) {
        return switch (phaseIndex) {
            case 0 -> List.of(
                    new ChoiceVO("A", "扩大副业"),
                    new ChoiceVO("B", "继续稳定上班"),
                    new ChoiceVO("C", "辞职创业"));
            case 1 -> List.of(
                    new ChoiceVO("A", "多陪伴家人"),
                    new ChoiceVO("B", "拓展社交圈"),
                    new ChoiceVO("C", "专注自我成长"));
            default -> List.of(
                    new ChoiceVO("A", "放慢节奏"),
                    new ChoiceVO("B", "保持冲劲"),
                    new ChoiceVO("C", "尝试新事物"));
        };
    }

    /**
     * 生成结局文案
     *
     * @param {LifeState} state - 最终状态
     * @param {String} playerName - 玩家姓名
     * @returns {String} 结局摘要
     */
    public String generateEndingSummary(LifeState state, String playerName) {
        ChildrenState children = state.getChildren();
        int childCount = state.childCount();
        int childAbility = children == null || children.getAbility() == null ? 0 : children.getAbility();
        int childAchievement = children == null || children.getAchievement() == null ? 0 : children.getAchievement();
        String familyBackground = state.getFamilyBackground() == null ? "未知家庭" : state.getFamilyBackground();
        return String.format(
                "%s的一生落下帷幕: 出身%s, 感情%d, 财富%d, 子女%d人(能力%d/成就%d), 权力%d, 名气%d, 健康%d, 享年%d岁。",
                playerName,
                familyBackground,
                safe(state.getAffection()),
                safe(state.getWealth()),
                childCount,
                childAbility,
                childAchievement,
                safe(state.getPower()),
                safe(state.getFame()),
                safe(state.getHealth()),
                safe(state.getLifespan()));
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private String resolvePhaseDesc(LifeState state) {
        if (Boolean.TRUE.equals(state.getGraduated()) || !isStudent(state)) {
            if (Boolean.TRUE.equals(state.getMarried())) {
                return "在工作与家庭之间寻找平衡";
            }
            return "在职场中摸索前行";
        }
        return "在大学校园里成长";
    }

    private boolean isStudent(LifeState state) {
        String occupation = state.getOccupation();
        return occupation != null && (occupation.contains("大学") || "大学生".equals(occupation));
    }

    private LifeState copyState(LifeState source) {
        LifeState target = new LifeState();
        target.setFamilyBackground(source.getFamilyBackground());
        target.setAffection(source.getAffection());
        target.setWealth(source.getWealth());
        target.setPower(source.getPower());
        target.setFame(source.getFame());
        target.setHealth(source.getHealth());
        target.setLifespan(source.getLifespan());
        target.setOccupation(source.getOccupation());
        if (source.getChildren() != null) {
            ChildrenState children = new ChildrenState();
            children.setCount(source.getChildren().getCount());
            children.setAbility(source.getChildren().getAbility());
            children.setAchievement(source.getChildren().getAchievement());
            target.setChildren(children);
        }
        target.setGraduated(source.getGraduated());
        target.setHasPartner(source.getHasPartner());
        target.setMarried(source.getMarried());
        target.setHasChild(source.getHasChild());
        if (source.getRecentEventCodes() != null) {
            target.setRecentEventCodes(new ArrayList<>(source.getRecentEventCodes()));
        }
        return target;
    }
}
