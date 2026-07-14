package com.poultry.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PoultryPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoultryPlatformApplication.class, args);
    }
}
