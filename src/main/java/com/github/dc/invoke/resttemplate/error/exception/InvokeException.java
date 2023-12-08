package com.github.dc.invoke.resttemplate.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * <p>
 * 接口调用异常
 * </p>
 *
 * @author wangpeiyuan
 * @date 2021/7/12 8:57
 */
public class InvokeException extends RuntimeException {

    /**
     * http状态码
     */
    private HttpStatus status;
    /**
     * 错误描述
     */
    private String message;
    /**
     * 返回数据
     */
    private Object response;

    public InvokeException(HttpStatus status, String message, Object response) {
        this.status = status;
        this.message = message;
        this.response = response;
        fillInStackTrace();
    }

    public InvokeException() {
        super();
    }

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeException(Throwable cause) {
        super(cause);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getResponse() {
        return response;
    }

    public String getMessage() {
        return this.status + StringUtils.SPACE + this.message + "外部系统返回：" + this.response;
    }
}
