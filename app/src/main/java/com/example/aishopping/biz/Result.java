package com.example.aishopping.biz;


// 一般响应体
public class Result<T> {
    private Integer code; // 状态码 0-失败 1-成功
    private String message; // 提示信息
    private T data; // 响应数据

    public Result(Integer code, String message, T data) {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
