package com.defvul.passets.api.bo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 说明:
 * 时间: 2019/11/11 11:17
 *
 * @author wimas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlBO {

    private String host;

    private List<String> urls;
}
