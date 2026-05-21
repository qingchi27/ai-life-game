package com.qingchi.ailife.controller;

import com.qingchi.ailife.common.Result;
import com.qingchi.ailife.dto.ChoiceReq;
import com.qingchi.ailife.dto.EndGameReq;
import com.qingchi.ailife.dto.StartGameReq;
import com.qingchi.ailife.service.IGameService;
import com.qingchi.ailife.vo.EndGameResp;
import com.qingchi.ailife.vo.GameResp;
import com.qingchi.ailife.vo.HistoryResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人生游戏接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Resource
    private IGameService gameService;

    /**
     * 开始人生
     *
     * @param {StartGameReq} req - 开始游戏请求
     * @returns {Result<GameResp>} 开局数据
     */
    @PostMapping("/start")
    public Result<GameResp> start(@Valid @RequestBody StartGameReq req) {
        return Result.success(gameService.start(req));
    }

    /**
     * 做选择推进剧情
     *
     * @param {ChoiceReq} req - 选择请求
     * @returns {Result<GameResp>} 推进后游戏状态
     */
    @PostMapping("/choice")
    public Result<GameResp> choice(@Valid @RequestBody ChoiceReq req) {
        return Result.success(gameService.choice(req));
    }

    /**
     * 获取当前人生状态
     *
     * @param {Long} id - 会话ID
     * @returns {Result<GameResp>} 当前游戏状态
     */
    @GetMapping("/session/{id}")
    public Result<GameResp> session(@PathVariable("id") Long id) {
        return Result.success(gameService.findSession(id));
    }

    /**
     * 主动结束人生
     *
     * @param {EndGameReq} req - 结束请求
     * @returns {Result<EndGameResp>} 结局信息
     */
    @PostMapping("/end")
    public Result<EndGameResp> end(@Valid @RequestBody EndGameReq req) {
        return Result.success(gameService.end(req));
    }

    /**
     * 人生轨迹
     *
     * @param {Long} id - 会话ID
     * @returns {Result<HistoryResp>} 历史步骤
     */
    @GetMapping("/history/{id}")
    public Result<HistoryResp> history(@PathVariable("id") Long id) {
        return Result.success(gameService.history(id));
    }
}
