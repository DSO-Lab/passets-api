package com.defvul.passets.api.bo.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 说明:
 * 时间: 2019/11/11 15:52
 *
 * @author wimas
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QueryUrlForm extends QueryBaseForm{
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
