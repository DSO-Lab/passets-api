package com.defvul.passets.api.bo.res;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 说明:
 * 时间: 2019/11/11 11:17
 *
 * @author wimas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "URL数据")
public class UrlBO {

    @ApiModelProperty(value = "主站")
    private String site;

    @ApiModelProperty(value = "访问次数")
    private long count;

    @ApiModelProperty(value = "子URL")
    private List<InfoBO> urls;
}
