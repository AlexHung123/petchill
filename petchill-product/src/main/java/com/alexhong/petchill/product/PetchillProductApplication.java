package com.alexhong.petchill.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.alexhong.petchill.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class PetchillProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillProductApplication.class, args);
    }

}
