package com.defvul.passets.api.bo.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 说明:
 * 时间: 2019/11/11 10:33
 *
 * @author wimas
 */
@Data
@ApiModel(description = "查询表单")
public class QueryBaseForm extends BaseForm{

    @ApiModelProperty(value = "开始时间")
    private Date start;

    @ApiModelProperty(value = "结束时间")
    private Date end;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "URL")
    private String url;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "指纹名称")
    private String finger;

    @ApiModelProperty(value = "是否内网")
    private boolean inner;

    @ApiModelProperty(value = "分类ID")
    @SerializedName("category_id")
    @JsonProperty("category_id")
    private List<Long> categoryId;
}

