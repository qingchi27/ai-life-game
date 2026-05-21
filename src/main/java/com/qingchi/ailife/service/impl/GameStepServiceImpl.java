package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.entity.GameStep;
import com.qingchi.ailife.mapper.IGameStepMapper;
import com.qingchi.ailife.service.IGameStepService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 游戏步骤服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Service
public class GameStepServiceImpl implements IGameStepService {

    @Resource
    private IGameStepMapper gameStepMapper;

    @Override
    public void saveStep(GameStep step) {
        gameStepMapper.insert(step);
    }

    @Override
    public List<GameStep> listBySessionId(Long sessionId) {
        return gameStepMapper.listBySessionId(sessionId);
    }
}
