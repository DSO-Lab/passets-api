package com.defvul.passets.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 说明:
 * 时间: 2019/11/11 15:13
 *
 * @author wimas
 */
@Data
public class ApplicationVO {
    private String name;

    private List<Map<String, String>> categories;

    private String product;

    private String confidence;

    private String version;
}
