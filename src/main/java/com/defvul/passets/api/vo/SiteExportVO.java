package com.defvul.passets.api.vo;

import com.github.crab2died.annotation.ExcelField;

import io.swagger.annotations.ApiModelProperty;
import joptsimple.internal.Strings;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class SiteExportVO {

    @ApiModelProperty(value = "网站地址")
    @ExcelField(title = "网站地址", order = 1)
    private String site;

    @ApiModelProperty(value = "IP")
    @ExcelField(title = "IP地址", order = 2)
    private String ip;

    @ApiModelProperty(value = "端口")
    @ExcelField(title = "端口", order = 3)
    private String port;

    @ApiModelProperty(value = "协议")
    @ExcelField(title = "协议", order = 4)
    private String service;

    @ApiModelProperty(value = "操作系统")
    @ExcelField(title = "操作系统", order = 5)
    private String os;

    @ApiModelProperty(value = "资产类类别")
    @ExcelField(title = "资产类类别", order = 6)
    private String assetsType;

    @ApiModelProperty(value = "资产服务类型")
    @ExcelField(title = "资产服务类型", order = 7)
    private String version;

    @ApiModelProperty(value = "资产类型设备")
    @ExcelField(title = "资产类型设备", order = 8)
    private String device;

    @ApiModelProperty(value = "证书信息")
    @ExcelField(title = "证书信息", order = 9)
    private Object certs;

    @ApiModelProperty(value = "地理位置")
    @ExcelField(title = "地理位置", order = 10)
    private String position;

    @ApiModelProperty(value = "经纬度")
    @ExcelField(title = "经纬度", order = 11)
    private String degree;

    @ApiModelProperty(value = "是否内网")
    @ExcelField(title = "内外网", order = 12)
    private String inner;

    @ApiModelProperty(value = "最后更新时间")
    @ExcelField(title = "最后更新时间", order = 13)
    private Date timestamp;

    @ApiModelProperty(value = "来源")
    @ExcelField(title = "来源", order = 14)
    private String tag;

    @ExcelField(title = "访问路径", order = 15)
    private String path;

    @ExcelField(title = "请求头", order = 16)
    private String header;

    @ExcelField(title = "状态码", order = 17)
    private String code;

    private Set<String> paths;

    private Set<String> nameVersion;

}

