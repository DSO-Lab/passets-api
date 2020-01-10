package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.GeoIpVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@ApiModel(description = "站点列表")
public class SiteBO {

    @ApiModelProperty(value = "热度")
    private long count;

    @ApiModelProperty(value = "主机地址")
    private String ip;

    @ApiModelProperty(value = "主机地址")
    private String host;

    @ApiModelProperty(value = "是否内网")
    private boolean inner;

    @ApiModelProperty(value = "状态码")
    private String code;

    @SerializedName("url_num")
    @ApiModelProperty(value = "子url数量")
    private Integer urlNum;

    @SerializedName("geoip")
    @JsonProperty("geoip")
    @ApiModelProperty(value = "地理位置")
    private GeoIpVO geoIp;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "站点")
    private String site;

    @ApiModelProperty(value = "指纹")
    private String app;

    @ApiModelProperty(value = "指纹集")
    private Set<String> apps;

    @SerializedName("site_type")
    @ApiModelProperty(value = "站点类型")
    private Set<String> siteType;

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "末次发现时间")
    private Date timestamp;

    @ApiModelProperty(value = "站点详情")
    private List<SiteInfoBO> sites;

    @ApiModelProperty(value = "原始响应头")
    private String header;

    @ApiModelProperty(value = "原始响正文")
    private String body;

}
