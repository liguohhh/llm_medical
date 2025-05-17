package edu.haut.baoxinyang.server.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应对象
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    private R() {
    }

    /**
     * 成功响应
     * @param <T> 数据类型
     * @return 成功的响应对象
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的响应对象
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("成功");
        r.setData(data);
        return r;
    }

    /**
     * 失败响应
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败的响应对象
     */
    public static <T> R<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败的响应对象
     */
    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
    
    /**
     * 设置响应数据
     * @param data 响应数据
     * @return 响应对象
     */
    public R<T> setData(T data) {
        this.data = data;
        return this;
    }
} 