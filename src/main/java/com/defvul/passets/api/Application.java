package com.defvul.passets.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明:
 * 时间: 2019/11/8 14:28
 *
 * @author wimas
 */
@SpringBootApplication(scanBasePackages = {"com.defvul"})
@EnableSwagger2
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Docket createRestApi() {
        List<Parameter> ap = new ArrayList<>();
        ParameterBuilder ab = new ParameterBuilder();
        ap.add(ab.name("X-Auth-Secret")
                .description("访问验证秘钥")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(true)
                .build()
        );
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                .globalOperationParameters(ap)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.defvul")).paths(PathSelectors.any()).build();
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
