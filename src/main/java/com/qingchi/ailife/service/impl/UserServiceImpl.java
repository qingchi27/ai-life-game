package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.entity.User;
import com.qingchi.ailife.mapper.IUserMapper;
import com.qingchi.ailife.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;

    @Override
    public User findUserById(Long id) {
        return userMapper.selectById(id);
    }
}
