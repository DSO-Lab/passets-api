package com.defvul.passets.api.bo.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class BaseForm {

    @SerializedName("page_size")
    @JsonProperty("page_size")
    private Integer pageSize;

    @SerializedName("current_page")
    @JsonProperty("current_page")
    private Integer currentPage;

    public Integer getPageSize() {
        return pageSize == 0 ? 10 : pageSize;
    }
}
