package com.qingchi.ailife.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 开始游戏请求
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class StartGameReq {

    /**
     * 玩家姓名
     **/
    @NotBlank(message = "玩家姓名不能为空")
    private String playerName;
}
