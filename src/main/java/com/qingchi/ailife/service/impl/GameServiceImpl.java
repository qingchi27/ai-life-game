package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.ai.StoryGenerator;
import com.qingchi.ailife.ai.dto.EndingContext;
import com.qingchi.ailife.ai.dto.NarrativeContext;
import com.qingchi.ailife.ai.dto.NarrativeResult;
import com.qingchi.ailife.common.ErrorCode;
import com.qingchi.ailife.common.ServiceExceptionUtil;
import com.qingchi.ailife.config.DeepSeekProperties;
import com.qingchi.ailife.config.GameProperties;
import com.qingchi.ailife.convertor.GameConvertor;
import com.qingchi.ailife.domain.GameStatusEnum;
import com.qingchi.ailife.domain.LifeState;
import com.qingchi.ailife.dto.ChoiceReq;
import com.qingchi.ailife.dto.EndGameReq;
import com.qingchi.ailife.dto.StartGameReq;
import com.qingchi.ailife.engine.GameEngine;
import com.qingchi.ailife.engine.GameResult;
import com.qingchi.ailife.entity.AiPromptLog;
import com.qingchi.ailife.entity.GameSession;
import com.qingchi.ailife.entity.GameStep;
import com.qingchi.ailife.service.IAiPromptLogService;
import com.qingchi.ailife.service.IGameService;
import com.qingchi.ailife.service.IGameSessionService;
import com.qingchi.ailife.service.IGameStepService;
import com.qingchi.ailife.service.INarrativeService;
import com.qingchi.ailife.util.JsonUtil;
import com.qingchi.ailife.vo.ChoiceVO;
import com.qingchi.ailife.vo.EndGameResp;
import com.qingchi.ailife.vo.GameResp;
import com.qingchi.ailife.vo.HistoryResp;
import com.qingchi.ailife.vo.HistoryStepVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 游戏核心服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Slf4j
@Service
public class GameServiceImpl implements IGameService {

    @Resource
    private IGameSessionService gameSessionService;

    @Resource
    private IGameStepService gameStepService;

    @Resource
    private IAiPromptLogService aiPromptLogService;

    @Resource
    private GameEngine gameEngine;

    @Resource
    private StoryGenerator storyGenerator;

    @Resource
    private INarrativeService narrativeService;

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private GameProperties gameProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GameResp start(StartGameReq req) {
        log.info("开始人生游戏, playerName: {}", req.getPlayerName());
        GameResult gameResult = gameEngine.start(req.getPlayerName());
        applyAiNarrative(req.getPlayerName(), 18, 1, null, gameResult, List.of());

        GameSession session = new GameSession();
        session.setUserId(gameProperties.getDefaultUserId());
        session.setSessionNo(UUID.randomUUID().toString().replace("-", ""));
        session.setPlayerName(req.getPlayerName());
        session.setCurrentAge(18);
        session.setCurrentStep(1);
        session.setLifeStatus(JsonUtil.toJson(gameResult.getState()));
        session.setCurrentStory(gameResult.getStory());
        session.setCurrentChoices(JsonUtil.toJson(gameResult.getChoices()));
        session.setGameStatus(GameStatusEnum.PLAYING.getCode());
        session.setScore(0);
        gameSessionService.saveSession(session);

        saveGameStep(session.getId(), 1, gameResult.getStory(), null,
                null, gameResult.getState(), "start");
        saveAiLogIfNeeded(session.getId(), "start", req.getPlayerName(), gameResult);

        log.info("人生游戏开局成功, sessionId: {}, step: {}", session.getId(), session.getCurrentStep());
        return buildGameResp(session, gameResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GameResp choice(ChoiceReq req) {
        log.info("推进人生剧情, sessionId: {}, choiceId: {}", req.getSessionId(), req.getChoiceId());
        GameSession session = requirePlayingSession(req.getSessionId());
        LifeState stateBefore = GameConvertor.parseLifeState(session.getLifeStatus());
        List<ChoiceVO> currentChoices = GameConvertor.parseChoices(session.getCurrentChoices());
        String choiceContent = resolveChoiceContent(currentChoices, req.getChoiceId());

        GameResult gameResult = gameEngine.nextStep(
                stateBefore, choiceContent, session.getCurrentStep(), session.getCurrentAge());

        int nextStep = session.getCurrentStep() + 1;
        List<String> historySummaries = buildHistorySummaries(session.getId());
        applyAiNarrative(session.getPlayerName(), session.getCurrentAge(), nextStep,
                choiceContent, gameResult, historySummaries);
        int nextAge = session.getCurrentAge() + gameResult.getAgeDelta();
        boolean reachMaxStep = nextStep >= gameProperties.getMaxStep();
        int lifespan = gameResult.getState().getLifespan() == null ? 120 : gameResult.getState().getLifespan();
        boolean naturalEnd = gameResult.isEnd() || reachMaxStep
                || (gameResult.getState().getHealth() != null && gameResult.getState().getHealth() <= 0)
                || nextAge >= lifespan;

        session.setCurrentStep(nextStep);
        session.setCurrentAge(nextAge);
        session.setLifeStatus(JsonUtil.toJson(gameResult.getState()));
        session.setCurrentStory(gameResult.getStory());
        session.setCurrentChoices(JsonUtil.toJson(gameResult.getChoices()));
        if (naturalEnd) {
            session.setGameStatus(GameStatusEnum.ENDED.getCode());
            session.setScore(calculateScore(gameResult.getState()));
            session.setEndingSummary(generateEndingSummaryWithAi(
                    session, gameResult.getState(), nextAge, gameResult.getStory(), choiceContent));
            gameResult.setEnd(true);
        }
        gameSessionService.updateSession(session);

        saveGameStep(session.getId(), nextStep, gameResult.getStory(), choiceContent,
                stateBefore, gameResult.getState(), gameResult.getEventType());
        saveAiLogIfNeeded(session.getId(), "choice", choiceContent, gameResult);

        log.info("人生剧情推进成功, sessionId: {}, step: {}, isEnd: {}",
                session.getId(), nextStep, naturalEnd);
        return buildGameResp(session, gameResult);
    }

    @Override
    public GameResp findSession(Long sessionId) {
        GameSession session = requireSession(sessionId);
        return GameConvertor.toGameResp(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EndGameResp end(EndGameReq req) {
        log.info("主动结束人生, sessionId: {}", req.getSessionId());
        GameSession session = requireSession(req.getSessionId());
        if (GameStatusEnum.ENDED.getCode() == session.getGameStatus()) {
            return buildEndResp(session);
        }
        LifeState state = GameConvertor.parseLifeState(session.getLifeStatus());
        session.setGameStatus(GameStatusEnum.ENDED.getCode());
        session.setScore(calculateScore(state));
        session.setEndingSummary(generateEndingSummaryWithAi(
                session, state, session.getCurrentAge(), session.getCurrentStory(), null));
        gameSessionService.updateSession(session);
        log.info("人生主动结束成功, sessionId: {}, score: {}", session.getId(), session.getScore());
        return buildEndResp(session);
    }

    @Override
    public HistoryResp history(Long sessionId) {
        requireSession(sessionId);
        List<GameStep> steps = gameStepService.listBySessionId(sessionId);
        HistoryResp resp = new HistoryResp();
        List<HistoryStepVO> stepList = new ArrayList<>();
        for (GameStep step : steps) {
            stepList.add(GameConvertor.toHistoryStepVO(step));
        }
        resp.setSteps(stepList);
        return resp;
    }

    private GameSession requireSession(Long sessionId) {
        GameSession session = gameSessionService.findSessionById(sessionId);
        if (session == null) {
            throw ServiceExceptionUtil.exception(ErrorCode.SESSION_NOT_FOUND);
        }
        return session;
    }

    private GameSession requirePlayingSession(Long sessionId) {
        GameSession session = requireSession(sessionId);
        if (GameStatusEnum.ENDED.getCode() == session.getGameStatus()) {
            throw ServiceExceptionUtil.exception(ErrorCode.SESSION_ENDED);
        }
        return session;
    }

    private String resolveChoiceContent(List<ChoiceVO> choices, String choiceId) {
        if (choices == null || choices.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCode.CHOICE_INVALID);
        }
        for (ChoiceVO choice : choices) {
            if (choiceId.equalsIgnoreCase(choice.getId())) {
                return choice.getContent();
            }
        }
        throw ServiceExceptionUtil.exception(ErrorCode.CHOICE_INVALID);
    }

    private void saveGameStep(Long sessionId, int stepNo, String story, String userChoice,
                              LifeState stateBefore, LifeState stateAfter, String eventType) {
        GameStep step = new GameStep();
        step.setSessionId(sessionId);
        step.setStepNo(stepNo);
        step.setStory(story);
        step.setUserChoice(userChoice);
        step.setStateBefore(stateBefore == null ? null : JsonUtil.toJson(stateBefore));
        step.setStateAfter(stateAfter == null ? null : JsonUtil.toJson(stateAfter));
        step.setEventType(eventType);
        gameStepService.saveStep(step);
    }

    private void saveAiLogIfNeeded(Long sessionId, String promptType, String prompt, GameResult result) {
        if (!result.isUseAi()) {
            return;
        }
        AiPromptLog aiLog = new AiPromptLog();
        aiLog.setSessionId(sessionId);
        aiLog.setPromptType(promptType);
        aiLog.setPromptContent(result.getAiPrompt() != null ? result.getAiPrompt() : prompt);
        aiLog.setResponseContent(result.getStory());
        aiLog.setTokenUsage(result.getTokenUsage() == null ? 0 : result.getTokenUsage());
        aiPromptLogService.saveLog(aiLog);
    }

    /**
     * 调用 DeepSeek 生成剧情, 逻辑与选项仍由引擎控制
     */
    private void applyAiNarrative(String playerName, int age, int step, String userChoice,
                                   GameResult gameResult, List<String> historySummaries) {
        String eventTitle = gameResult.getEvent() != null ? gameResult.getEvent().getTitle() : "人生事件";
        String eventType = gameResult.getEventType() == null ? "life" : gameResult.getEventType();
        boolean opening = step <= 1 && (userChoice == null || userChoice.isBlank());

        NarrativeContext context = NarrativeContext.builder()
                .playerName(playerName)
                .age(age)
                .step(step)
                .userChoice(userChoice)
                .eventTitle(eventTitle)
                .eventType(eventType)
                .eventTemplate(gameResult.getStory())
                .state(gameResult.getState())
                .historySummaries(historySummaries)
                .opening(opening)
                .build();

        NarrativeResult narrative = narrativeService.generate(context);
        gameResult.setStory(narrative.getText());
        gameResult.setUseAi(narrative.isFromAi());
        gameResult.setAiPrompt(narrative.getPrompt());
        gameResult.setTokenUsage(narrative.getTokenUsage());
    }

    private List<String> buildHistorySummaries(Long sessionId) {
        List<GameStep> steps = gameStepService.listBySessionId(sessionId);
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }
        int limit = deepSeekProperties.getHistoryLimit() == null ? 5 : deepSeekProperties.getHistoryLimit();
        int from = Math.max(0, steps.size() - limit);
        List<String> summaries = new ArrayList<>();
        for (int i = from; i < steps.size(); i++) {
            GameStep step = steps.get(i);
            String choice = step.getUserChoice();
            String story = step.getStory();
            if (story == null || story.isBlank()) {
                continue;
            }
            String brief = story.length() > 60 ? story.substring(0, 60) + "..." : story;
            if (choice != null && !choice.isBlank()) {
                summaries.add("选择「" + choice + "」后: " + brief);
            } else {
                summaries.add(brief);
            }
        }
        return summaries;
    }

    /**
     * 调用 DeepSeek 生成结局摘要, 失败时回退模板
     */
    private String generateEndingSummaryWithAi(GameSession session, LifeState state, int age,
                                               String latestStory, String latestChoice) {
        String template = storyGenerator.generateEndingSummary(state, session.getPlayerName());
        int score = calculateScore(state);
        String title = resolveEndingTitle(state);
        List<String> history = appendLatestHistory(
                buildHistorySummaries(session.getId()), latestStory, latestChoice);

        EndingContext context = EndingContext.builder()
                .playerName(session.getPlayerName())
                .age(age)
                .score(score)
                .endingTitle(title)
                .templateSummary(template)
                .state(state)
                .historySummaries(history)
                .build();

        NarrativeResult result = narrativeService.generateEnding(context);
        saveEndingAiLog(session.getId(), result);
        return result.getText();
    }

    private List<String> appendLatestHistory(List<String> history, String latestStory, String latestChoice) {
        if (latestStory == null || latestStory.isBlank()) {
            return history;
        }
        List<String> extended = new ArrayList<>(history);
        String brief = latestStory.length() > 80 ? latestStory.substring(0, 80) + "..." : latestStory;
        if (latestChoice != null && !latestChoice.isBlank()) {
            extended.add("最后选择「" + latestChoice + "」: " + brief);
        } else {
            extended.add("最后: " + brief);
        }
        return extended;
    }

    private void saveEndingAiLog(Long sessionId, NarrativeResult result) {
        if (!result.isFromAi()) {
            return;
        }
        AiPromptLog aiLog = new AiPromptLog();
        aiLog.setSessionId(sessionId);
        aiLog.setPromptType("end");
        aiLog.setPromptContent(result.getPrompt());
        aiLog.setResponseContent(result.getText());
        aiLog.setTokenUsage(result.getTokenUsage());
        aiPromptLogService.saveLog(aiLog);
    }

    private GameResp buildGameResp(GameSession session, GameResult gameResult) {
        GameResp resp = GameConvertor.toGameResp(session);
        if (gameResult.getEvent() != null) {
            resp.setEvent(gameResult.getEvent());
        }
        if (gameResult.isEnd()) {
            resp.setIsEnd(true);
        }
        return resp;
    }

    private EndGameResp buildEndResp(GameSession session) {
        EndGameResp resp = new EndGameResp();
        LifeState state = GameConvertor.parseLifeState(session.getLifeStatus());
        resp.setEndingTitle(resolveEndingTitle(state));
        resp.setSummary(session.getEndingSummary());
        resp.setScore(session.getScore());
        resp.setTags(resolveTags(state));
        return resp;
    }

    private String resolveEndingTitle(LifeState state) {
        if (state == null) {
            return "平凡的一生";
        }
        if (state.getWealth() != null && state.getWealth() > 100000) {
            return "财富显赫的一生";
        }
        if (state.getPower() != null && state.getPower() >= 80) {
            return "权势滔天的一生";
        }
        if (state.getFame() != null && state.getFame() >= 80) {
            return "名满天下的一生";
        }
        if (state.getHealth() != null && state.getHealth() < 30) {
            return "透支健康的一生";
        }
        return "平凡但稳定的一生";
    }

    private List<String> resolveTags(LifeState state) {
        List<String> tags = new ArrayList<>();
        if (state == null) {
            tags.add("普通");
            return tags;
        }
        if (state.getWealth() != null && state.getWealth() > 50000) {
            tags.add("富足");
        } else {
            tags.add("稳定");
        }
        if (state.getHealth() != null && state.getHealth() >= 70) {
            tags.add("健康");
        }
        if (state.getFame() != null && state.getFame() >= 60) {
            tags.add("知名");
        }
        if (state.childCount() > 0) {
            tags.add("有后");
        }
        if (state.getOccupation() != null && state.getOccupation().contains("创业")) {
            tags.add("冒险");
        } else {
            tags.add("踏实");
        }
        return tags;
    }

    private int calculateScore(LifeState state) {
        if (state == null) {
            return 60;
        }
        int wealthScore = Math.min(20, (state.getWealth() == null ? 0 : state.getWealth()) / 5000);
        int healthScore = (state.getHealth() == null ? 50 : state.getHealth()) / 5;
        int fameScore = (state.getFame() == null ? 30 : state.getFame()) / 5;
        int affectionScore = (state.getAffection() == null ? 40 : state.getAffection()) / 5;
        int powerScore = (state.getPower() == null ? 20 : state.getPower()) / 5;
        int childScore = 0;
        if (state.getChildren() != null) {
            int ability = state.getChildren().getAbility() == null ? 0 : state.getChildren().getAbility();
            int achievement = state.getChildren().getAchievement() == null ? 0 : state.getChildren().getAchievement();
            childScore = Math.min(15, (ability + achievement) / 10 + state.childCount() * 2);
        }
        return Math.min(100, wealthScore + healthScore + fameScore + affectionScore + powerScore + childScore);
    }
}
