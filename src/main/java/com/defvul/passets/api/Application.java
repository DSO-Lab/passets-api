package com.defvul.passets.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 说明:
 * 时间: 2019/11/8 14:28
 *
 * @author wimas
 */
@SpringBootApplication(scanBasePackages = {"com.defvul"})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
