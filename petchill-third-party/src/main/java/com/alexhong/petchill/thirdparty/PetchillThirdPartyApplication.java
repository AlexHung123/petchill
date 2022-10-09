package com.alexhong.petchill.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class PetchillThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillThirdPartyApplication.class, args);
    }

}
