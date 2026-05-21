package com.qingchi.ailife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户选择请求
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class ChoiceReq {

    /**
     * 会话ID
     **/
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 选项ID
     **/
    @NotBlank(message = "选项ID不能为空")
    private String choiceId;
}
