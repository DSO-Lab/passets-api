package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "数据详情")
public class HostListBO {

    @ApiModelProperty(value = "访问次数")
    private long count;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "数据类型")
    private String pro;

    @ApiModelProperty(value = "指纹")
    private List<ApplicationVO> apps;

    @ApiModelProperty(value = "网站标题", notes = "HTTP类型数据")
    private String title;

    @SerializedName("min_date")
    @ApiModelProperty(value = "首次发现时间")
    private Date minDate;

    @SerializedName("max_date")
    @ApiModelProperty(value = "最后发现时间")
    private Date maxDate;

    @ApiModelProperty(value = "响应头")
    private String header;

    @ApiModelProperty(value = "响应正文")
    private String body;

    @ApiModelProperty(value = "原始payload")
    private String payload;


}


