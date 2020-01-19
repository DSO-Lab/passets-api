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
@ApiModel(description = "重要站点")
public class MajorSiteBO {

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "站点分类")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "热度")
    private long count;

    @ApiModelProperty(value = "url")
    private String url;

    @ApiModelProperty(value = "站点")
    private String site;

    @ApiModelProperty(value = "组件")
    private String assembly;

    @ApiModelProperty(value = "主机地址")
    private String host;

    @SerializedName("@timestamp")
    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "末次发现时间")
    private Date timestamp;

    @SerializedName("url_tpl")
    @JsonProperty("url_tpl")
    @ApiModelProperty(value = "url")
    private String urlTpl;
}
