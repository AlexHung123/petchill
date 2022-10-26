package com.alexhong.petchill.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.configuration.CompatibilityVerifierAutoConfiguration;

@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, CompatibilityVerifierAutoConfiguration.class})
public class PetchillSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetchillSearchApplication.class, args);
	}

}
