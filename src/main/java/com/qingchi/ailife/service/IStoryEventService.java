package com.qingchi.ailife.service;

import com.qingchi.ailife.entity.StoryEvent;

import java.util.List;

/**
 * 剧情事件服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IStoryEventService {

    StoryEvent findByEventCode(String eventCode);

    List<StoryEvent> listByAge(Integer age);
}
