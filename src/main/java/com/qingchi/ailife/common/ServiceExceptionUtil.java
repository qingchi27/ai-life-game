package com.qingchi.ailife.common;

/**
 * 业务异常工具类
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
public final class ServiceExceptionUtil {

    private ServiceExceptionUtil() {
    }

    public static ServiceException exception(ErrorCode errorCode) {
        return new ServiceException(errorCode);
    }
}
