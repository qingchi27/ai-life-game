package com.qingchi.ailife.service;

import com.qingchi.ailife.dto.ChoiceReq;
import com.qingchi.ailife.dto.EndGameReq;
import com.qingchi.ailife.dto.StartGameReq;
import com.qingchi.ailife.vo.EndGameResp;
import com.qingchi.ailife.vo.GameResp;
import com.qingchi.ailife.vo.HistoryResp;

/**
 * 游戏核心服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IGameService {

    GameResp start(StartGameReq req);

    GameResp choice(ChoiceReq req);

    GameResp findSession(Long sessionId);

    EndGameResp end(EndGameReq req);

    HistoryResp history(Long sessionId);
}
