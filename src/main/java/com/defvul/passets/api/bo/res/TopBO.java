package com.defvul.passets.api.bo.res;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "资产关键词top")
public class TopBO {

    @ApiModelProperty(value = "搜索类型")
    private List<TopInfoBO> pros;

    @ApiModelProperty(value = "指纹排行")
    private List<TopInfoBO> apps;

    @ApiModelProperty(value = "内外网")
    private List<TopInfoBO> inners;

    @ApiModelProperty(value = "端口排行")
    private List<TopInfoBO> ports;

    @ApiModelProperty(value = "国家")
    private List<TopInfoBO> country;

    @ApiModelProperty(value = "操作系统")
    private List<TopInfoBO> os;

}
