package com.alexhong.petchill.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.alexhong.petchill.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class PetchillMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillMemberApplication.class, args);
    }

}
