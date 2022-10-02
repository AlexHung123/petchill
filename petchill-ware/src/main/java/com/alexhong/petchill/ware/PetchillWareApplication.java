package com.alexhong.petchill.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PetchillWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillWareApplication.class, args);
    }

}
