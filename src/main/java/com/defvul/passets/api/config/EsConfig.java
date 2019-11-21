package com.defvul.passets.api.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 说明:
 * 时间: 2019/11/8 14:36
 *
 * @author wimas
 */
@Configuration
public class EsConfig {

    @Value("${elasticsearch.url}")
    private String url;

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create(url))
        );
    }
}
