package com.qingchi.ailife.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Getter
public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
