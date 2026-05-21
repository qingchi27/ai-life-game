package com.qingchi.ailife.mapper;

import com.qingchi.ailife.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Mapper
public interface IUserMapper {

    User selectById(@Param("id") Long id);

    int insert(User user);
}
