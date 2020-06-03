package com.defvul.passets.api.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 说明:
 * 时间: 2019/12/23 11:31
 *
 * @author wimas
 */
public class DateUtil {
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYYMMDD = "yyyyMMdd";

    public static String format(Date time) {
        return format(time, YYYY_MM_DD);
    }

    public static String format(Date time, String format) {
        SimpleDateFormat f = new SimpleDateFormat(format);
        return f.format(time);
    }

    public static Date add(Date date, Integer n) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, n);
        return c.getTime();
    }


}
