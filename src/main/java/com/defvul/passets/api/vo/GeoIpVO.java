package com.defvul.passets.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "地址信息")
public class GeoIpVO {

    @SerializedName("city_name")
    @JsonProperty("city_name")
    @ApiModelProperty(value = "城市")
    private String cityName;

    @SerializedName("country_name")
    @JsonProperty("country_name")
    @ApiModelProperty(value = "国家")
    private String countryName;

    @ApiModelProperty(value = "经纬度")
    private LocationVO location;

}