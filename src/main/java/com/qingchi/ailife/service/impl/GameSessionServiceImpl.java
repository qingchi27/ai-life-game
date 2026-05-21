package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.entity.GameSession;
import com.qingchi.ailife.mapper.IGameSessionMapper;
import com.qingchi.ailife.service.IGameSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 游戏会话服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Service
public class GameSessionServiceImpl implements IGameSessionService {

    @Resource
    private IGameSessionMapper gameSessionMapper;

    @Override
    public GameSession findSessionById(Long id) {
        return gameSessionMapper.selectById(id);
    }

    @Override
    public void saveSession(GameSession session) {
        gameSessionMapper.insert(session);
    }

    @Override
    public void updateSession(GameSession session) {
        gameSessionMapper.updateById(session);
    }
}
