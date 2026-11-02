package com.cafeos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CafeOsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CafeOsApplication.class, args);
    }
}
