package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 说明:
 * 时间: 2019/12/13 18:20
 *
 * @author wimas
 */
@Data
@ApiModel(description = "数据详情")
public class InfoBO extends BaseInfoBO {

    @ApiModelProperty(value = "首次发现时间")
    private Date minAsString;

    @ApiModelProperty(value = "最后发现时间")
    private Date maxAsString;

}
