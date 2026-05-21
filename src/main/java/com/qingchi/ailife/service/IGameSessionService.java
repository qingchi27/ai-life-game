package com.qingchi.ailife.service;

import com.qingchi.ailife.entity.GameSession;

/**
 * 游戏会话服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IGameSessionService {

    GameSession findSessionById(Long id);

    void saveSession(GameSession session);

    void updateSession(GameSession session);
}
