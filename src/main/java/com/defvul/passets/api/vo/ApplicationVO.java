package com.defvul.passets.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 说明:
 * 时间: 2019/11/11 15:13
 *
 * @author wimas
 */
@Data
@ApiModel(description = "指纹信息")
public class ApplicationVO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "分类")
    private List<CategoryVO> categories;
    @ApiModelProperty(value = "可信度")
    private String confidence;

    @ApiModelProperty(value = "版本")
    private String version;

    @ApiModelProperty(value = "厂家", notes = "TCP类型")
    private String product;

    @ApiModelProperty(value = "操作系统", notes = "TCP类型")
    private String os;

    @ApiModelProperty(value = "服务", notes = "TCP类型")
    private String service;

    @ApiModelProperty(value = "设备", notes = "TCP类型")
    private String device;

    @ApiModelProperty(value = "其他信息", notes = "TCP类型")
    private String info;

    @Override
    public boolean equals(Object obj) {
        return name.equals(((ApplicationVO) obj).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
