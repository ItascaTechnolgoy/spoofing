package com.itasca.spoofing;

import com.itasca.spoofing.service.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpoofingApplication implements CommandLineRunner {

    @Autowired
    private DataInitializationService dataInitializationService;

    public static void main(String[] args) {
        SpringApplication.run(SpoofingApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        dataInitializationService.initializeDefaultData();
    }
}
