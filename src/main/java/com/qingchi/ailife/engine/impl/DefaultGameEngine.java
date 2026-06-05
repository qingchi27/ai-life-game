package com.qingchi.ailife.engine.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qingchi.ailife.ai.StoryGenerator;
import com.qingchi.ailife.domain.ChildrenState;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.engine.GameEngine;
import com.qingchi.ailife.engine.GameResult;
import com.qingchi.ailife.engine.LifeStateEffectApplier;
import com.qingchi.ailife.engine.StoryEventFilter;
import com.qingchi.ailife.entity.StoryEvent;
import com.qingchi.ailife.mapper.IStoryEventMapper;
import com.qingchi.ailife.util.JsonUtil;
import com.qingchi.ailife.vo.ChoiceVO;
import com.qingchi.ailife.vo.GameEventVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 默认游戏引擎实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Component
public class DefaultGameEngine implements GameEngine {

    private static final List<String> KEY_EVENT_KEYWORDS = List.of(
            "创业", "破产", "恋爱", "转职", "副业", "结婚", "离婚"
    );

    private static final Set<String> PARTNER_START_EVENTS = Set.of(
            "CAMPUS_LOVE", "LOVE_MEET", "OFFICE_ROMANCE", "BLIND_DATE", "LONG_DISTANCE"
    );

    @Resource
    private IStoryEventMapper storyEventMapper;

    @Resource
    private StoryGenerator storyGenerator;

    @Override
    public GameResult start(String playerName) {
        return storyGenerator.generateOpening(playerName);
    }

    @Override
    public GameResult nextStep(LifeState state, String choiceContent, int currentStep, int currentAge) {
        LifeState workingState = copyState(state);
        applyChoiceEffect(workingState, choiceContent);
        updateFlagsFromChoice(workingState, choiceContent);

        boolean keyEvent = isKeyEvent(currentStep, choiceContent);
        GameResult result;
        if (keyEvent) {
            result = storyGenerator.generateKeyStory(workingState, choiceContent, currentAge);
            result.setUseAi(true);
        } else {
            result = pickLocalEvent(workingState, currentAge);
            result.setUseAi(false);
        }
        if (result.getEvent() == null && result.getEventType() != null) {
            GameEventVO event = new GameEventVO();
            event.setType(result.getEventType());
            event.setTitle(result.getStory() != null && result.getStory().length() > 20
                    ? result.getStory().substring(0, 20) + "..."
                    : "人生事件");
            result.setEvent(event);
        }
        return result;
    }

    private GameResult pickLocalEvent(LifeState state, int currentAge) {
        List<StoryEvent> events = storyEventMapper.listByAge(currentAge);
        GameResult result = new GameResult();
        LifeState newState = copyState(state);
        result.setState(newState);
        result.setAgeDelta(1);

        List<StoryEvent> eligible = filterEligibleEvents(events, newState, currentAge);
        if (eligible.isEmpty()) {
            result.setStory(buildFallbackStory(newState, currentAge));
            result.setChoices(defaultChoices());
            result.setEventType("life");
            return result;
        }

        StoryEvent event = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
        result.setStory(event.getEventContent());
        result.setEventType(event.getEventType());
        result.setChoices(buildChoicesFromEvent(event));

        GameEventVO eventVo = new GameEventVO();
        eventVo.setType(event.getEventType());
        eventVo.setTitle(event.getTitle());
        result.setEvent(eventVo);

        applyEventEffect(newState, event.getEffect());
        updateFlagsFromEvent(newState, event.getEventCode());
        newState.recordRecentEvent(event.getEventCode());
        return result;
    }

    private List<StoryEvent> filterEligibleEvents(List<StoryEvent> events, LifeState state, int age) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        return events.stream()
                .filter(event -> StoryEventFilter.canTrigger(event, state, age))
                .collect(Collectors.toList());
    }

    private String buildFallbackStory(LifeState state, int age) {
        if (inStudentPhase(state)) {
            return String.format("%d岁的你按部就班地上课、复习, 校园生活平淡而充实...", age);
        }
        if (Boolean.TRUE.equals(state.getMarried())) {
            return String.format("%d岁的你和家人把日子过得踏实, 没有惊天动地的大事...", age);
        }
        return String.format("%d岁的你专注工作与生活, 这一年平稳度过...", age);
    }

    private void applyChoiceEffect(LifeState state, String choiceContent) {
        if (choiceContent == null || choiceContent.isBlank()) {
            return;
        }
        if ("开始副业".equals(choiceContent) || "接副业".equals(choiceContent) || "扩大副业".equals(choiceContent)) {
            state.setWealth(safeAdd(state.getWealth(), 1500));
            state.setFame(clamp(safeAdd(state.getFame(), 5), 0, 100));
            state.setOccupation("副业达人");
            state.setPower(clamp(safeAdd(state.getPower(), 5), 0, 100));
        } else if ("辞职创业".equals(choiceContent) || "全力投入".equals(choiceContent)) {
            state.setWealth(safeAdd(state.getWealth(), -2000));
            state.setFame(clamp(safeAdd(state.getFame(), 10), 0, 100));
            state.setPower(clamp(safeAdd(state.getPower(), 15), 0, 100));
            state.setOccupation("创业者");
        } else if ("躺平".equals(choiceContent) || "躺平休息".equals(choiceContent) || "顺其自然".equals(choiceContent)) {
            state.setHealth(clamp(safeAdd(state.getHealth(), 8), 0, 100));
            state.setWealth(safeAdd(state.getWealth(), -500));
            state.setLifespan(safeAdd(state.getLifespan(), 1));
        } else if ("努力学习".equals(choiceContent)) {
            state.setFame(clamp(safeAdd(state.getFame(), 5), 0, 100));
            state.setHealth(clamp(safeAdd(state.getHealth(), -2), 0, 100));
            state.setPower(clamp(safeAdd(state.getPower(), 3), 0, 100));
        } else if ("努力工作".equals(choiceContent) || "继续上班".equals(choiceContent)
                || "继续稳定上班".equals(choiceContent) || "继续努力".equals(choiceContent)) {
            state.setWealth(safeAdd(state.getWealth(), 800));
            state.setHealth(clamp(safeAdd(state.getHealth(), -3), 0, 100));
            state.setPower(clamp(safeAdd(state.getPower(), 4), 0, 100));
        } else if ("学习AI".equals(choiceContent)) {
            state.setOccupation("AI开发者");
            state.setFame(clamp(safeAdd(state.getFame(), 8), 0, 100));
            state.setPower(clamp(safeAdd(state.getPower(), 10), 0, 100));
        } else if ("换个方向".equals(choiceContent)) {
            state.setFame(clamp(safeAdd(state.getFame(), 5), 0, 100));
            state.setAffection(clamp(safeAdd(state.getAffection(), 3), 0, 100));
        } else if ("继续硬扛".equals(choiceContent) || "硬撑".equals(choiceContent)) {
            state.setWealth(safeAdd(state.getWealth(), 500));
            state.setHealth(clamp(safeAdd(state.getHealth(), -8), 0, 100));
            state.setLifespan(safeAdd(state.getLifespan(), -1));
        } else if ("开始调理".equals(choiceContent) || "看医生".equals(choiceContent)) {
            state.setWealth(safeAdd(state.getWealth(), -800));
            state.setHealth(clamp(safeAdd(state.getHealth(), 10), 0, 100));
            state.setLifespan(safeAdd(state.getLifespan(), 2));
        } else if ("表白试试".equals(choiceContent) || "主动追求".equals(choiceContent)) {
            state.setHasPartner(true);
            state.setAffection(clamp(safeAdd(state.getAffection(), 15), 0, 100));
        } else if ("浪漫求婚".equals(choiceContent) || "简单领证".equals(choiceContent)) {
            state.setHasPartner(true);
            state.setMarried(true);
            state.setAffection(clamp(safeAdd(state.getAffection(), 10), 0, 100));
        } else if ("潇洒放手".equals(choiceContent) || "协议离婚".equals(choiceContent)) {
            state.setHasPartner(false);
            state.setMarried(false);
            state.setAffection(clamp(safeAdd(state.getAffection(), -10), 0, 100));
        } else if ("考研深造".equals(choiceContent)) {
            state.setGraduated(true);
            state.setOccupation("研究生");
            state.setPower(clamp(safeAdd(state.getPower(), 8), 0, 100));
        } else if ("直接就业".equals(choiceContent)) {
            state.setGraduated(true);
            state.setOccupation("职场新人");
            state.setPower(clamp(safeAdd(state.getPower(), 6), 0, 100));
        }
    }

    private void updateFlagsFromChoice(LifeState state, String choiceContent) {
        if (choiceContent == null) {
            return;
        }
        if ("接受实习".equals(choiceContent) || "开心入职".equals(choiceContent)) {
            if (inStudentPhase(state)) {
                state.setOccupation("实习生");
            }
        }
    }

    private void updateFlagsFromEvent(LifeState state, String eventCode) {
        if (eventCode == null) {
            return;
        }
        if (PARTNER_START_EVENTS.contains(eventCode)) {
            state.setHasPartner(true);
        }
        if ("MARRIAGE_PROPOSE".equals(eventCode)) {
            state.setHasPartner(true);
        }
        if ("WEDDING".equals(eventCode)) {
            state.setHasPartner(true);
            state.setMarried(true);
        }
        if ("DIVORCE".equals(eventCode)) {
            state.setMarried(false);
            state.setHasPartner(false);
        }
        if ("BREAKUP".equals(eventCode)) {
            state.setHasPartner(false);
        }
        if ("CHILD_BORN".equals(eventCode)) {
            ChildrenState children = state.ensureChildren();
            children.setCount(safeAdd(children.getCount(), 1));
            children.setAbility(clamp(safeAdd(children.getAbility(), 25), 0, 100));
            state.setHasChild(true);
        }
        if ("CHILD_EDUCATION".equals(eventCode) || "CHILD_COLLEGE".equals(eventCode)) {
            ChildrenState children = state.ensureChildren();
            children.setAchievement(clamp(safeAdd(children.getAchievement(), 12), 0, 100));
            children.setAbility(clamp(safeAdd(children.getAbility(), 5), 0, 100));
        }
        if ("GRADUATE_COLLEGE".equals(eventCode) || "FIRST_JOB".equals(eventCode)) {
            state.setGraduated(true);
            if ("大学生".equals(state.getOccupation())) {
                state.setOccupation("职场新人");
            }
        }
        if ("PROMOTION".equals(eventCode) || "MANAGER_ROLE".equals(eventCode)) {
            state.setPower(clamp(safeAdd(state.getPower(), 10), 0, 100));
        }
        if ("CONFERENCE_SPEAK".equals(eventCode) || "VIRAL_VIDEO".equals(eventCode) || "MEDIA_INTERVIEW".equals(eventCode)) {
            state.setFame(clamp(safeAdd(state.getFame(), 8), 0, 100));
        }
    }

    private void applyEventEffect(LifeState state, String effectJson) {
        if (effectJson == null || effectJson.isBlank()) {
            return;
        }
        Map<String, Object> effect = JsonUtil.fromJson(effectJson, new TypeReference<>() {});
        LifeStateEffectApplier.apply(state, effect);
    }

    private List<ChoiceVO> buildChoicesFromEvent(StoryEvent event) {
        List<String> raw = JsonUtil.fromJson(event.getChoices(), new TypeReference<>() {});
        if (raw == null || raw.isEmpty()) {
            return defaultChoices();
        }
        List<ChoiceVO> choices = new ArrayList<>();
        char id = 'A';
        for (String content : raw) {
            choices.add(new ChoiceVO(String.valueOf(id), content));
            id++;
        }
        return choices;
    }

    private List<ChoiceVO> defaultChoices() {
        return List.of(
                new ChoiceVO("A", "继续努力"),
                new ChoiceVO("B", "换个方向"),
                new ChoiceVO("C", "顺其自然")
        );
    }

    private boolean isKeyEvent(int step, String choiceContent) {
        if (step % 5 == 0) {
            return true;
        }
        if (choiceContent == null) {
            return false;
        }
        return KEY_EVENT_KEYWORDS.stream().anyMatch(choiceContent::contains);
    }

    private boolean inStudentPhase(LifeState state) {
        if (Boolean.TRUE.equals(state.getGraduated())) {
            return false;
        }
        String occupation = state.getOccupation();
        return occupation == null || occupation.contains("大学") || "大学生".equals(occupation);
    }

    private LifeState copyState(LifeState source) {
        LifeState target = new LifeState();
        target.setAffection(source.getAffection());
        target.setWealth(source.getWealth());
        target.setPower(source.getPower());
        target.setFame(source.getFame());
        target.setHealth(source.getHealth());
        target.setLifespan(source.getLifespan());
        target.setFamilyBackground(source.getFamilyBackground());
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

    private int safeAdd(Integer value, int delta) {
        int base = value == null ? 0 : value;
        return base + delta;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
