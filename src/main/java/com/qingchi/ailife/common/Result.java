package com.qingchi.ailife.common;

import lombok.Data;

/**
 * 统一接口返回结构
 *
 * @author hengji-chen
 * @date 2026/5/20
 */
@Data
public class Result<T> {

    /**
     * 状态码
     **/
    private Integer code;

    /**
     * 提示信息
     **/
    private String message;

    /**
     * 业务数据
     **/
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
