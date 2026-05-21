package com.qingchi.ailife.engine;

import com.qingchi.ailife.domain.LifeState;

/**
 * 游戏引擎接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface GameEngine {

    /**
     * 生成开局
     *
     * @param {String} playerName - 玩家姓名
     * @returns {GameResult} 开局结果
     */
    GameResult start(String playerName);

    /**
     * 推进下一步
     *
     * @param {LifeState} state - 当前状态
     * @param {String} choiceContent - 用户选择内容
     * @param {int} currentStep - 当前步数
     * @param {int} currentAge - 当前年龄
     * @returns {GameResult} 推进结果
     */
    GameResult nextStep(LifeState state, String choiceContent, int currentStep, int currentAge);
}
