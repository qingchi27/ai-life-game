package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.ai.StoryGenerator;
import com.qingchi.ailife.common.ErrorCode;
import com.qingchi.ailife.common.ServiceExceptionUtil;
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
    private GameProperties gameProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GameResp start(StartGameReq req) {
        log.info("开始人生游戏, playerName: {}", req.getPlayerName());
        GameResult gameResult = gameEngine.start(req.getPlayerName());

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
        int nextAge = session.getCurrentAge() + gameResult.getAgeDelta();
        boolean reachMaxStep = nextStep >= gameProperties.getMaxStep();
        boolean naturalEnd = gameResult.isEnd() || reachMaxStep
                || (gameResult.getState().getHealth() != null && gameResult.getState().getHealth() <= 0);

        session.setCurrentStep(nextStep);
        session.setCurrentAge(nextAge);
        session.setLifeStatus(JsonUtil.toJson(gameResult.getState()));
        session.setCurrentStory(gameResult.getStory());
        session.setCurrentChoices(JsonUtil.toJson(gameResult.getChoices()));
        if (naturalEnd) {
            session.setGameStatus(GameStatusEnum.ENDED.getCode());
            session.setScore(calculateScore(gameResult.getState()));
            session.setEndingSummary(storyGenerator.generateEndingSummary(gameResult.getState(), session.getPlayerName()));
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
        String summary = storyGenerator.generateEndingSummary(state, session.getPlayerName());
        session.setEndingSummary(summary);
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
        aiLog.setPromptContent(prompt);
        aiLog.setResponseContent(result.getStory());
        aiLog.setTokenUsage(0);
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
        if (state.getMoney() != null && state.getMoney() > 100000) {
            return "财富自由的一生";
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
        if (state.getMoney() != null && state.getMoney() > 50000) {
            tags.add("富足");
        } else {
            tags.add("稳定");
        }
        if (state.getHealth() != null && state.getHealth() >= 70) {
            tags.add("健康");
        }
        if ("创业者".equals(state.getCareer())) {
            tags.add("冒险");
        } else {
            tags.add("普通");
        }
        return tags;
    }

    private int calculateScore(LifeState state) {
        if (state == null) {
            return 60;
        }
        int moneyScore = Math.min(30, (state.getMoney() == null ? 0 : state.getMoney()) / 3000);
        int healthScore = (state.getHealth() == null ? 50 : state.getHealth()) / 2;
        int luckScore = (state.getLuck() == null ? 50 : state.getLuck()) / 5;
        int relationScore = (state.getRelationship() == null ? 40 : state.getRelationship()) / 5;
        return Math.min(100, moneyScore + healthScore + luckScore + relationScore);
    }
}
