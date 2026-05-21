package com.qingchi.ailife.service;

import com.qingchi.ailife.entity.AiPromptLog;

/**
 * AI提示词日志服务接口
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public interface IAiPromptLogService {

    void saveLog(AiPromptLog log);
}
