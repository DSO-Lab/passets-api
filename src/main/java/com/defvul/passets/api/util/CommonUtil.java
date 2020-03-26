package com.defvul.passets.api.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明:
 * 时间: 2020/3/18 17:26
 *
 * @author wimas
 */
public class CommonUtil {
    public static final String REGEX_PORT = "^([1-9]|[1-5]?[0-9]{2,4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[1-5])$";

    public static boolean isPort(String port) {
        if (StringUtils.isBlank(port)) {
            return false;
        }
        Pattern pattern = Pattern.compile(REGEX_PORT);
        Matcher matcher = pattern.matcher(port);
        return matcher.find();
    }
}
