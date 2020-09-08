package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.defvul.passets.api.vo.GeoIpVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class SiteExportBO {

    @ApiModelProperty(value = "网站地址")
    private String site;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "指纹")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "证书信息")
    private String certs;

    @SerializedName("geoip")
    @JsonProperty("geoip")
    @ApiModelProperty(value = "地理位置")
    private GeoIpVO geoIp;

    @ApiModelProperty(value = "是否内网", notes = "HTTP类型数据")
    private boolean inner;

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "最后更新时间")
    private Date timestamp;

    @ApiModelProperty(value = "来源")
    private String tag;

    private String header;

    private String code;

    private Set<String> headers;

    private Set<String> codes;

    private Set<String> paths;
}

