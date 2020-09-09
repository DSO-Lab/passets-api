package com.defvul.passets.api.vo;

import com.github.crab2died.annotation.ExcelField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class HostExportVO {

    @ExcelField(title = "IP地址", order = 1)
    private String ip;

    @ExcelField(title = "端口", order = 2)
    private String port;

    @ExcelField(title = "协议", order = 3)
    private String service;

    @ExcelField(title = "操作系统", order = 4)
    private String os;

    @ExcelField(title = "资产类类别", order = 5)
    private String assetsType;

    @ExcelField(title = "资产服务类型", order = 6)
    private String version;

    @ExcelField(title = "资产类型设备", order = 7)
    private String device;

    @ExcelField(title = "证书信息", order = 8)
    private Object certs;

    @ExcelField(title = "地理位置", order = 9)
    private String position;

    @ExcelField(title = "经纬度", order = 10)
    private String degree;

    @ExcelField(title = "内外网", order = 11)
    private String inner;

    @ExcelField(title = "最后更新时间", order = 12)
    private String timestamp;

    @ExcelField(title = "来源", order = 13)
    private String tag;

    private Set<String> nameVersion = new HashSet<>();

}
