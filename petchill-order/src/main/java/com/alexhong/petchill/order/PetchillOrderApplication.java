package com.alexhong.petchill.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PetchillOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetchillOrderApplication.class, args);
    }

}
