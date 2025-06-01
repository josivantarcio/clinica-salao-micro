package com.clinicsalon.professional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProfessionalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfessionalServiceApplication.class, args);
    }
}
