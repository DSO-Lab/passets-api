package com.defvul.passets.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 说明:
 * 时间: 2019/12/13 19:00
 *
 * @author wimas
 */
@Data
@ApiModel(description = "分类信息")
public class CategoryVO {

    @ApiModelProperty(value = "分类名称")
    private String name;

    @ApiModelProperty(value = "分类ID")
    private String id;
}
