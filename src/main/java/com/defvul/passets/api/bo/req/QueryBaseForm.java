package com.defvul.passets.api.bo.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

/**
 * 说明:
 * 时间: 2019/11/11 10:33
 *
 * @author wimas
 */
@Data
public class QueryBaseForm {

    private Date start;

    private Date end;

    @SerializedName("only_inner")
    @JsonProperty("only_inner")
    private boolean onlyInner = false;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isOnlyInner() {
        return onlyInner;
    }

    public void setOnlyInner(boolean onlyInner) {
        this.onlyInner = onlyInner;
    }
}
