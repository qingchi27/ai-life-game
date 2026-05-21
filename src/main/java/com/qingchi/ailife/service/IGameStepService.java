package com.qingchi.ailife.service;

import com.qingchi.ailife.entity.GameStep;

import java.util.List;

/**
 * 游戏步骤服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IGameStepService {

    void saveStep(GameStep step);

    List<GameStep> listBySessionId(Long sessionId);
}
