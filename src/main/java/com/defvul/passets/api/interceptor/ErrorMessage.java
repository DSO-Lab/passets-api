package com.defvul.passets.api.interceptor;

import java.util.Date;
/**
 * 说明:
 * 时间: 2020/2/11 17:00
 *
 * @author wimas
 */
public class ErrorMessage {
    private Date timestamp;

    private Integer status;

    private String error;

    private Object message;

    private String path;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
