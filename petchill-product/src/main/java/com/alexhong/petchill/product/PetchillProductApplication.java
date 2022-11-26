package com.alexhong.petchill.product;

import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRedissonHttpSession
@EnableFeignClients(basePackages = "com.alexhong.petchill.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class PetchillProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillProductApplication.class, args);
    }

}
