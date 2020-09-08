package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.defvul.passets.api.vo.GeoIpVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class HostExportBO {

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "指纹")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "证书信息")
    private Object certs;

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
}
