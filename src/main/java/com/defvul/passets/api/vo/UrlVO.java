package com.defvul.passets.api.vo;

import lombok.Data;

/**
 * 说明:
 * 时间: 2019/11/11 15:44
 *
 * @author wimas
 */
@Data
public class UrlVO {
    private String url;

    private String fragment;

    private String host;

    private String path;

    private String query;

    private String scheme;
}
