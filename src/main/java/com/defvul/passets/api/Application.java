package com.defvul.passets.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;

/**
 * 说明:
 * 时间: 2019/11/8 14:28
 *
 * @author wimas
 */
@SpringBootApplication(scanBasePackages = {"com.defvul"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Passets")
                .description("Passets API")
                .version("1.0.0")
                .build();
    }
}
