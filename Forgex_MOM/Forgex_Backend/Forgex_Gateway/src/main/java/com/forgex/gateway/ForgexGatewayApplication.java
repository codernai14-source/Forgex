package com.forgex.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.forgex.gateway")
@EnableDiscoveryClient
public class ForgexGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForgexGatewayApplication.class, args);
    }
}
