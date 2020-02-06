package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "基本数据详情")
public class BaseInfoBO {

    @ApiModelProperty(value = "访问次数")
    private long count;

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "访问时间")
    private Date timestamp;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "数据类型")
    private String pro;

    @ApiModelProperty(value = "IP+端口")
    private String host;

    @SerializedName("ip_num")
    @JsonProperty("ip_num")
    private long ipNum;

    @ApiModelProperty(value = "指纹")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "类型")
    private String type;

    // 以下为http类型数据

    @ApiModelProperty(value = "状态码", notes = "HTTP类型数据")
    private String code;

    @ApiModelProperty(value = "URL", notes = "HTTP类型数据")
    private String url;

    @ApiModelProperty(value = "Server", notes = "HTTP类型数据")
    private String server;

    @ApiModelProperty(value = "http请求正文", notes = "HTTP类型数据")
    private String body;

    @SerializedName("url_tpl")
    @JsonProperty("url_tpl")
    @ApiModelProperty(value = "url模板", notes = "HTTP类型数据")
    private String urlTpl;

    @ApiModelProperty(value = "站点", notes = "HTTP类型数据")
    private String site;

    @ApiModelProperty(value = "访问路径", notes = "HTTP类型数据")
    private String path;

    @ApiModelProperty(value = "是否内网", notes = "HTTP类型数据")
    private boolean inner;

    @ApiModelProperty(value = "请求头", notes = "HTTP类型数据")
    private String header;

    @ApiModelProperty(value = "网站标题", notes = "HTTP类型数据")
    private String title;
}
