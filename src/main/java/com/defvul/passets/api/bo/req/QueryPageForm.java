package com.defvul.passets.api.bo.req;

import com.defvul.passets.api.vo.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 说明:
 * 时间: 2020/3/18 17:42
 *
 * @author wimas
 */
@Data
public class QueryPageForm extends QueryBaseForm {
    @SerializedName("page_size")
    @JsonProperty("page_size")
    private Integer pageSize;

    @SerializedName("current_page")
    @JsonProperty("current_page")
    private Integer currentPage;

    public Integer getPageSize() {
        return pageSize == null || pageSize <= 0 ? 10 : pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage == null || currentPage <= 0 ? 1 : currentPage;
    }

    public <T> Page<T> toPage() {
        Page<T> page = new Page<>();
        page.setCurrentPage(getCurrentPage());
        page.setPageSize(getPageSize());
        return page;
    }
}
