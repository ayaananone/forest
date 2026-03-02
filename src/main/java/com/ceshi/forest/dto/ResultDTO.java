package com.ceshi.forest.dto;

import lombok.Data;

@Data
public class ResultDTO<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public ResultDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功 - 带数据
    public static <T> ResultDTO<T> ok(T data) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功 - 带数据和消息
    public static <T> ResultDTO<T> ok(T data, String message) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 成功 - 仅消息
    public static ResultDTO<Void> okMsg(String message) {
        ResultDTO<Void> result = new ResultDTO<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    // 失败
    public static <T> ResultDTO<T> fail(String message) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> ResultDTO<T> fail(Integer code, String message) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}