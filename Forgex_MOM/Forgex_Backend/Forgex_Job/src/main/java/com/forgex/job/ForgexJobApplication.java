package com.forgex.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.forgex.job")
public class ForgexJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForgexJobApplication.class, args);
    }
}

