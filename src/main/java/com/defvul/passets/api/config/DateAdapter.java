package com.defvul.passets.api.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 说明:
 * 时间: 2020/3/26 16:11
 *
 * @author wimas
 */
public class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Date date = null;
        try {
            date = format.parse(jsonElement.getAsString());
        } catch (Exception e) {
            try {
                date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(jsonElement.getAsString());
            } catch (Exception ee) {
                date = new Date(jsonElement.getAsJsonPrimitive().getAsLong());
            }
        }
        return date;
    }

    @Override
    public JsonElement serialize(Date src, Type type, JsonSerializationContext jsonSerializationContext) {
        if (src == null) {
            return new JsonPrimitive("");
        } else {
            return new JsonPrimitive(format.format(src));
        }
    }
}
