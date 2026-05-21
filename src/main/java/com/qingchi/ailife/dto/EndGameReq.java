package com.qingchi.ailife.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 结束游戏请求
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class EndGameReq {

    /**
     * 会话ID
     **/
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}
