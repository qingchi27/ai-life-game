package com.qingchi.ailife.ai.prompt;

import com.qingchi.ailife.ai.dto.EndingContext;
import com.qingchi.ailife.ai.dto.NarrativeContext;
import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.LifeState;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DeepSeek 叙事 Prompt 构建器
 *
 * @author hengji-chen
 * @date 2026/6/5
 */
@Component
public class NarrativePromptBuilder {

    private static final String SYSTEM_PROMPT = """
            你是一名人生叙事 AI 作家, 负责为「AI 人生模拟器」撰写剧情段落。
            规则:
            1. 只输出剧情正文, 不要输出 JSON, 不要列出状态数值变化
            2. 语言生动有代入感, 使用第二人称「你」
            3. 必须自然衔接玩家历史与本次事件, 提及事件名称
            4. 150-250 字, 不要分段标题, 不要列表
            5. 不得改变游戏逻辑, 不得虚构与模板矛盾的结局
            """;

    private static final String ENDING_SYSTEM_PROMPT = """
            你是一名人生叙事 AI 作家, 负责为「AI 人生模拟器」撰写人生结局回顾。
            规则:
            1. 只输出结局正文, 不要输出 JSON, 不要列表
            2. 语言温暖有总结感, 使用第二人称「你」或第三人称玩家名
            3. 必须涵盖模板中的关键人生数据(感情, 财富, 子女, 权力, 名气, 健康, 享年)
            4. 200-350 字, 一段连贯文字
            5. 不得虚构与给定数据矛盾的成就或经历
            """;

    /**
     * 构建系统提示词
     *
     * @returns {String} system prompt
     */
    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * 构建用户提示词
     *
     * @param {NarrativeContext} context - 叙事上下文
     * @returns {String} user prompt
     */
    public String buildUserPrompt(NarrativeContext context) {
        LifeState state = context.getState();
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下信息生成一段剧情:\n\n");
        sb.append("玩家: ").append(nullToEmpty(context.getPlayerName())).append('\n');
        sb.append("年龄: ").append(safeInt(context.getAge())).append(" 岁\n");
        sb.append("步数: 第 ").append(safeInt(context.getStep())).append(" 回合\n\n");
        sb.append("当前状态:\n");
        sb.append(formatState(state));
        sb.append('\n');

        if (context.getUserChoice() != null && !context.getUserChoice().isBlank()) {
            sb.append("用户上一步选择: ").append(context.getUserChoice()).append('\n');
        }
        if (context.isOpening()) {
            sb.append("场景: 人生开局\n");
        } else {
            sb.append("触发事件: ").append(nullToEmpty(context.getEventTitle())).append('\n');
            sb.append("事件类型: ").append(nullToEmpty(context.getEventType())).append('\n');
        }
        if (context.getEventTemplate() != null && !context.getEventTemplate().isBlank()) {
            sb.append("事件要点(请在此基础上扩写, 勿偏离): ").append(context.getEventTemplate()).append('\n');
        }

        List<String> history = context.getHistorySummaries();
        if (history != null && !history.isEmpty()) {
            sb.append("\n玩家近期经历:\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append(i + 1).append(". ").append(history.get(i)).append('\n');
            }
        }
        sb.append("\n请生成剧情文本:");
        return sb.toString();
    }

    /**
     * 构建结局系统提示词
     *
     * @returns {String} system prompt
     */
    public String buildEndingSystemPrompt() {
        return ENDING_SYSTEM_PROMPT;
    }

    /**
     * 构建结局用户提示词
     *
     * @param {EndingContext} context - 结局上下文
     * @returns {String} user prompt
     */
    public String buildEndingUserPrompt(EndingContext context) {
        LifeState state = context.getState();
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下信息撰写人生结局回顾:\n\n");
        sb.append("玩家: ").append(nullToEmpty(context.getPlayerName())).append('\n');
        sb.append("享年约: ").append(safeInt(context.getAge())).append(" 岁\n");
        sb.append("人生评分: ").append(safeInt(context.getScore())).append(" 分\n");
        sb.append("结局标题: ").append(nullToEmpty(context.getEndingTitle())).append("\n\n");
        sb.append("最终状态:\n");
        sb.append(formatState(state));
        sb.append("\n数据摘要(必须体现在文中): ").append(nullToEmpty(context.getTemplateSummary())).append('\n');

        List<String> history = context.getHistorySummaries();
        if (history != null && !history.isEmpty()) {
            sb.append("\n人生重要经历:\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append(i + 1).append(". ").append(history.get(i)).append('\n');
            }
        }
        sb.append("\n请生成结局摘要:");
        return sb.toString();
    }

    private String formatState(LifeState state) {
        if (state == null) {
            return "- 状态未知\n";
        }
        ChildrenState children = state.getChildren();
        int childCount = state.childCount();
        int childAbility = children == null || children.getAbility() == null ? 0 : children.getAbility();
        String occupation = state.getOccupation() == null ? "未知" : state.getOccupation();
        String family = state.getFamilyBackground() == null ? "普通家庭" : state.getFamilyBackground();
        return String.format(
                "- 职业: %s\n- 家庭背景: %s\n- 感情: %d\n- 财富: %d\n- 权力: %d\n- 名气: %d\n- 健康: %d\n- 子女: %d 人(能力 %d)\n",
                occupation,
                family,
                safeInt(state.getAffection()),
                safeInt(state.getWealth()),
                safeInt(state.getPower()),
                safeInt(state.getFame()),
                safeInt(state.getHealth()),
                childCount,
                childAbility);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
