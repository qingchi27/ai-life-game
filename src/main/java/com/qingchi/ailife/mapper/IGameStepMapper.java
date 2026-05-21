package com.qingchi.ailife.mapper;

import com.qingchi.ailife.entity.GameStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 游戏步骤Mapper
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Mapper
public interface IGameStepMapper {

    int insert(GameStep step);

    List<GameStep> listBySessionId(@Param("sessionId") Long sessionId);
}
