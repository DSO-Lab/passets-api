package com.defvul.passets.api.bo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 说明: 详情查询表单
 * 时间: 2020/3/18 17:06
 *
 * @author wimas
 */
@Data
@ApiModel(description = "详情查询表单")
public class QueryInfoForm {
    @ApiModelProperty(value = "查询内容")
    private String value;

    @ApiModelProperty(value = "开始时间")
    private Date start;

    @ApiModelProperty(value = "结束时间")
    private Date end;

    public QueryBaseForm toBaseForm(boolean isHost) {
        QueryBaseForm baseForm = new QueryBaseForm();
        if (isHost) {
            baseForm.setIp(value);
        } else {
            baseForm.setSite(value);
        }
        baseForm.setStart(start);
        baseForm.setEnd(end);
        return baseForm;
    }
}
