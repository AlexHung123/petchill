package com.alexhong.petchill.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1. create config
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.1.120:6379");
        //2. create new instance
        return Redisson.create(config);
    }
}
