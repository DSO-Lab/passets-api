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

    @ApiModelProperty(value = "url模板")
    @SerializedName("url_tpl")
    private String urlTpl;

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "末次发现时间")
    private Date timestamp;


}
