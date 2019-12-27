package com.defvul.passets.api.bo.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class BaseForm {

    @SerializedName("ps")
    @JsonProperty("ps")
    private Integer pageSize;

    @SerializedName("pi")
    @JsonProperty("pi")
    private Integer currentPage;

}
