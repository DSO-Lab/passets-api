package com.defvul.passets.api.bo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 说明:
 * 时间: 2019/11/8 15:39
 *
 * @author wimas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpPortBO {

    private String ip;

    private String port;
}
