package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.entity.StoryEvent;
import com.qingchi.ailife.mapper.IStoryEventMapper;
import com.qingchi.ailife.service.IStoryEventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 剧情事件服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Service
public class StoryEventServiceImpl implements IStoryEventService {

    @Resource
    private IStoryEventMapper storyEventMapper;

    @Override
    public StoryEvent findByEventCode(String eventCode) {
        return storyEventMapper.selectByEventCode(eventCode);
    }

    @Override
    public List<StoryEvent> listByAge(Integer age) {
        return storyEventMapper.listByAge(age);
    }
}
