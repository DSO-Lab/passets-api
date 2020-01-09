package com.defvul.passets.api.bo.res;

import com.defvul.passets.api.vo.ApplicationVO;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
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

    private List<ApplicationVO> apps;


}
