package com.defvul.passets.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JacksonJsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JacksonJsonUtils() {
    }

    public static String toJsonStr(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException var2) {
            throw new RuntimeException(var2);
        }
    }

    public static <T> T fromJsonStr(String jsonStr, Class<T> tClass) {
        try {
            T result = objectMapper.readValue(jsonStr, tClass);
            return result;
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static <T> T fromJsonStr(String jsonStr, ParameterHolder<T> holder) {
        try {
            return objectMapper.readValue(jsonStr, holder);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static <T> T fronJsonFile(File file, Class<T> tClass) {
        try {
            return objectMapper.readValue(file, tClass);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static <T> T fronJsonFile(File file, ParameterHolder<T> holder) {
        try {
            return objectMapper.readValue(file, holder);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static <T> T fronJsonStream(InputStream is, Class<T> tClass) {
        try {
            return objectMapper.readValue(is, tClass);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static <T> T fronJsonStream(InputStream is, ParameterHolder<T> holder) {
        try {
            return objectMapper.readValue(is, holder);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}