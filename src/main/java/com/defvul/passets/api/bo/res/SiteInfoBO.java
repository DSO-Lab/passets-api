package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "子url列表")
public class SiteInfoBO {

    private String path;

    @SerializedName("min_date")
    private Date minDate;

    @SerializedName("max_date")
    private Date maxDate;

    @ApiModelProperty(value = "指纹信息")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "原始响应头")
    private String header;

    @ApiModelProperty(value = "原始响正文")
    private String body;

    @ApiModelProperty(value = "原始url")
    private String url;

}
