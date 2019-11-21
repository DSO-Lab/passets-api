package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;
import java.util.Date;

/**
 * 说明:
 * 时间: 2019/11/11 15:10
 *
 * @author wimas
 */
@Data
@ApiModel
public class UrlInfoBO {

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    private Date timestamp;

    @SerializedName("_id")
    @JsonProperty("_id")
    private String id;

    private List<ApplicationVO> applications;

    @SerializedName("content_type")
    @JsonProperty("content_type")
    private String contentType;

    private String dst;

    @SerializedName("dst_addr")
    @JsonProperty("dst_addr")
    private String dstAddr;

    @SerializedName("dst_inner")
    @JsonProperty("dst_inner")
    private boolean dstInner;

    @SerializedName("dst_num")
    @JsonProperty("dst_num")
    private long dstNum;

    @SerializedName("dst_port")
    @JsonProperty("dst_port")
    private String dstPort;

    @SerializedName("http_server")
    @JsonProperty("http_server")
    private String httpServer;

    @SerializedName("http_uri")
    @JsonProperty("http_uri")
    private String httpUri;

    private String protocol;

    @SerializedName("response_body")
    @JsonProperty("response_body")
    private String responseBody;

    @SerializedName("response_code")
    @JsonProperty("response_code")
    private int responseCode;

    @SerializedName("response_headers")
    @JsonProperty("response_headers")
    private String responseHeaders;

    private String src;

    @SerializedName("src_addr")
    @JsonProperty("src_addr")
    private String srcAddr;

    @SerializedName("src_inner")
    @JsonProperty("src_inner")
    private boolean srcInner;

    @SerializedName("src_num")
    @JsonProperty("src_num")
    private long srcNum;

    @SerializedName("src_port")
    @JsonProperty("src_port")
    private String srcPort;

    private String title;
}
