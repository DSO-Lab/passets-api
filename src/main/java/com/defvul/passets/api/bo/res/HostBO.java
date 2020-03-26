package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.GeoIpVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "主机资产列表")
public class HostBO {

    // 基本数据

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "是否内网")
    private Boolean inner;

    @SerializedName("geoip")
    @JsonProperty("geoip")
    @ApiModelProperty(value = "地理位置")
    private GeoIpVO geoIp;

    // 端口数据

    @ApiModelProperty(value = "主机端口信息")
    private List<HostInfoBO> hosts;

    // 详情分类

    @ApiModelProperty(value = "端口")
    private List<String> ports;

    @ApiModelProperty(value = "组件")
    private List<TopInfoBO> assembly;

    @ApiModelProperty(value = "协议")
    private List<TopInfoBO> services;

}
