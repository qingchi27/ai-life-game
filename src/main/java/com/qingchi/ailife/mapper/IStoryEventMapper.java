package com.qingchi.ailife.mapper;

import com.qingchi.ailife.entity.StoryEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 剧情事件Mapper
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Mapper
public interface IStoryEventMapper {

    StoryEvent selectByEventCode(@Param("eventCode") String eventCode);

    List<StoryEvent> listByAge(@Param("age") Integer age);
}
