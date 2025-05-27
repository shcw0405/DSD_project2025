package com.example.patientmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class PatientManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientManagementSystemApplication.class, args);
    }
}
