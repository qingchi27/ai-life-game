package com.qingchi.ailife.mapper;

import com.qingchi.ailife.entity.AiPromptLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI提示词日志Mapper
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Mapper
public interface IAiPromptLogMapper {

    int insert(AiPromptLog log);
}
