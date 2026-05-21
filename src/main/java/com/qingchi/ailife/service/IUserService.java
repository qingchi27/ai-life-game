package com.qingchi.ailife.service;

import com.qingchi.ailife.entity.User;

/**
 * 用户服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IUserService {

    User findUserById(Long id);
}
