package com.qingchi.ailife.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingchi.ailife.common.ErrorCode;
import com.qingchi.ailife.common.ServiceExceptionUtil;

/**
 * JSON工具类
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw ServiceExceptionUtil.exception(ErrorCode.PARAM_ERROR);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw ServiceExceptionUtil.exception(ErrorCode.PARAM_ERROR);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw ServiceExceptionUtil.exception(ErrorCode.PARAM_ERROR);
        }
    }
}
