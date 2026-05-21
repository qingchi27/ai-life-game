package com.qingchi.ailife.mapper;

import com.qingchi.ailife.entity.GameSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 游戏会话Mapper
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Mapper
public interface IGameSessionMapper {

    GameSession selectById(@Param("id") Long id);

    int insert(GameSession session);

    int updateById(GameSession session);
}
