package com.qingchi.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class User {

    /**
     * 主键
     **/
    private Long id;

    /**
     * 昵称
     **/
    private String nickname;

    /**
     * 微信OpenID
     **/
    private String openId;

    /**
     * 头像地址
     **/
    private String avatar;

    /**
     * 创建时间
     **/
    private LocalDateTime createdTime;

    /**
     * 更新时间
     **/
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除
     **/
    private Integer deleted;
}
