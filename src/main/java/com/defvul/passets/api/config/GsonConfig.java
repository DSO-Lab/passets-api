package com.defvul.passets.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import springfox.documentation.spring.web.json.Json;

import java.util.Arrays;

@Configuration
public class GsonConfig {

    @Value("${spring.gson.date-format}")
    private String GSON_DATE_FORMAT;

    @Bean
    public Gson gson() {
        return new GsonBuilder().setDateFormat(this.GSON_DATE_FORMAT).enableComplexMapKeySerialization()
                .registerTypeAdapter(Json.class, new SpringFoxJsonToGsonAdapter()).create();
    }

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson());
        return new HttpMessageConverters(true, Arrays.asList(new HttpMessageConverter[]{gsonHttpMessageConverter}));
    }
}
