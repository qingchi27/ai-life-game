package com.qingchi.ailife.engine.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qingchi.ailife.ai.StoryGenerator;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.engine.GameEngine;
import com.qingchi.ailife.engine.GameResult;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * 默认游戏引擎实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Component
public class DefaultGameEngine implements GameEngine {

    private static final List<String> KEY_EVENT_TYPES = List.of(
            "创业", "破产", "恋爱", "转职", "结局", "career", "relationship", "startup"
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
        boolean keyEvent = isKeyEvent(currentStep, choiceContent);
        GameResult result;
        if (keyEvent) {
            result = storyGenerator.generateKeyStory(state, choiceContent, currentStep);
            result.setUseAi(true);
        } else {
            result = pickLocalEvent(state, currentAge);
            result.setUseAi(false);
        }
        applyChoiceEffect(state, choiceContent, result);
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

        if (events == null || events.isEmpty()) {
            result.setStory("平淡的一年过去了, 生活没有太大波澜...");
            result.setChoices(defaultChoices());
            result.setEventType("life");
            return result;
        }

        StoryEvent event = events.get(ThreadLocalRandom.current().nextInt(events.size()));
        result.setStory(event.getEventContent());
        result.setEventType(event.getEventType());
        result.setChoices(buildChoicesFromEvent(event));

        GameEventVO eventVo = new GameEventVO();
        eventVo.setType(event.getEventType());
        eventVo.setTitle(event.getTitle());
        result.setEvent(eventVo);

        applyEventEffect(newState, event.getEffect());
        return result;
    }

    private void applyChoiceEffect(LifeState state, String choiceContent, GameResult result) {
        LifeState target = result.getState() != null ? result.getState() : state;
        if ("开始副业".equals(choiceContent) || "接副业".equals(choiceContent) || "扩大副业".equals(choiceContent)) {
            target.setMoney(safeAdd(target.getMoney(), 1500));
            target.setLuck(safeAdd(target.getLuck(), 5));
            target.setCareer("副业达人");
        } else if ("辞职创业".equals(choiceContent) || "全力投入".equals(choiceContent)) {
            target.setMoney(safeAdd(target.getMoney(), -2000));
            target.setLuck(safeAdd(target.getLuck(), 10));
            target.setCareer("创业者");
        } else if ("躺平".equals(choiceContent) || "躺平休息".equals(choiceContent)) {
            target.setHealth(safeAdd(target.getHealth(), 8));
            target.setMoney(safeAdd(target.getMoney(), -500));
        } else if ("努力工作".equals(choiceContent) || "继续上班".equals(choiceContent) || "继续稳定上班".equals(choiceContent)) {
            target.setMoney(safeAdd(target.getMoney(), 800));
            target.setHealth(safeAdd(target.getHealth(), -3));
        } else if ("学习AI".equals(choiceContent)) {
            target.setCareer("AI开发者");
            target.setLuck(safeAdd(target.getLuck(), 8));
        }
        result.setState(target);
    }

    private void applyEventEffect(LifeState state, String effectJson) {
        if (effectJson == null || effectJson.isBlank()) {
            return;
        }
        Map<String, Object> effect = JsonUtil.fromJson(effectJson, new TypeReference<>() {});
        if (effect == null) {
            return;
        }
        applyNumericEffect(state, effect, "money");
        applyNumericEffect(state, effect, "health");
        applyNumericEffect(state, effect, "luck");
        applyNumericEffect(state, effect, "relationship");
        if (effect.get("career") != null) {
            state.setCareer(String.valueOf(effect.get("career")));
        }
    }

    private void applyNumericEffect(LifeState state, Map<String, Object> effect, String key) {
        if (!effect.containsKey(key) || effect.get(key) == null) {
            return;
        }
        int delta = Integer.parseInt(String.valueOf(effect.get(key)));
        switch (key) {
            case "money" -> state.setMoney(safeAdd(state.getMoney(), delta));
            case "health" -> state.setHealth(clamp(safeAdd(state.getHealth(), delta), 0, 100));
            case "luck" -> state.setLuck(clamp(safeAdd(state.getLuck(), delta), 0, 100));
            case "relationship" -> state.setRelationship(clamp(safeAdd(state.getRelationship(), delta), 0, 100));
            default -> {
            }
        }
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
        return KEY_EVENT_TYPES.stream().anyMatch(choiceContent::contains);
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

    private int safeAdd(Integer value, int delta) {
        int base = value == null ? 0 : value;
        return base + delta;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
