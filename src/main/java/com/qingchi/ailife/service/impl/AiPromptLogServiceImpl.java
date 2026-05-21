package com.qingchi.ailife.service.impl;

import com.qingchi.ailife.entity.AiPromptLog;
import com.qingchi.ailife.mapper.IAiPromptLogMapper;
import com.qingchi.ailife.service.IAiPromptLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * AI提示词日志服务实现
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Service
public class AiPromptLogServiceImpl implements IAiPromptLogService {

    @Resource
    private IAiPromptLogMapper aiPromptLogMapper;

    @Override
    public void saveLog(AiPromptLog log) {
        aiPromptLogMapper.insert(log);
    }
}
