package com.defvul.passets.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;


import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "分页对象")
public class Page<T> {

    @ApiModelProperty(value = "总条数")
    private Integer total = 0;

    @SerializedName("page_size")
    @JsonProperty("page_size")
    @ApiModelProperty(value = "每页最大条数")
    private Integer pageSize;

    @SerializedName("current_page")
    @JsonProperty("current_page")
    @ApiModelProperty(value = "当前页数")
    private Integer currentPage;

    @ApiModelProperty(value = "数据列表")
    private List<T> data = new ArrayList<>();

    @SerializedName("total_page")
    @JsonProperty("total_page")
    @ApiModelProperty(value = "总页数")
    private Integer totalPage;

    @Bean
    public Integer getTotalPage() {
        return this.total != null && this.total > 0 ? (this.total + this.pageSize - 1) / this.pageSize : 0;
    }

}
