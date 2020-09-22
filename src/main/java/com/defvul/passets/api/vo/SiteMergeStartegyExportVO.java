package com.defvul.passets.api.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SiteMergeStartegyExportVO {

    @ApiModelProperty(value = "网站地址")
    @ExcelProperty(value = "网站地址", index = 0)
    private String site;

    @ApiModelProperty(value = "IP")
    @ExcelProperty(value = "IP地址", index = 1)
    private String ip;

    @ApiModelProperty(value = "端口")
    @ExcelProperty(value = "端口", index = 2)
    private String port;

    @ApiModelProperty(value = "操作系统")
    @ExcelProperty(value = "操作系统", index = 3)
    private String os;

    @ApiModelProperty(value = "资产类类别")
    @ExcelProperty(value = "资产类类别", index = 4)
    private String assetsType;

    @ApiModelProperty(value = "资产服务类型")
    @ExcelProperty(value = "资产服务类型", index = 5)
    private String version;

    @ApiModelProperty(value = "资产类型设备")
    @ExcelProperty(value = "资产类型设备", index = 6)
    private String device;

    @ApiModelProperty(value = "地理位置")
    @ExcelProperty(value = "地理位置", index = 7)
    private String position;

    @ApiModelProperty(value = "经纬度")
    @ExcelProperty(value = "经纬度", index = 8)
    private String degree;

    @ApiModelProperty(value = "是否内网")
    @ExcelProperty(value = "内外网", index = 9)
    private String inner;

    @ApiModelProperty(value = "最后更新时间")
    @ExcelProperty(value = "最后更新时间", index = 10)
    private String timestamp;

    @ApiModelProperty(value = "来源")
    @ExcelProperty(value = "来源", index = 11)
    private String tag;

    @ExcelProperty(value= "访问路径", index = 12)
    private String path;

    @ExcelProperty(value = "请求头", index = 13)
    private String header;

    @ExcelProperty(value = "状态码", index = 14)
    private String code;
}
