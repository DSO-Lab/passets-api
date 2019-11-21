package com.defvul.passets.api.common.bean;


import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.Map;

public class LogBO {
    @SerializedName("start_time")
    private Date startTime;
    @SerializedName("end_time")
    private Date endTime;
    @SerializedName("create_by")
    private Object createBy;
    @SerializedName("request_url")
    private String requestUrl;
    @SerializedName("remote_address")
    private String remoteAddress;
    @SerializedName("user_agent")
    private String userAgent;
    private String title;
    private String content;
    private String method;
    private String params;
    private String exception;
    private Map<String, String> headers;
    @SerializedName("response_status")
    private int responseStatus;
    @SerializedName("response_size")
    private int responseSize;

    public LogBO() {
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Object getCreateBy() {
        return this.createBy;
    }

    public void setCreateBy(Object createBy) {
        this.createBy = createBy;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getException() {
        return this.exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getResponseStatus() {
        return this.responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public int getResponseSize() {
        return this.responseSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }
}
