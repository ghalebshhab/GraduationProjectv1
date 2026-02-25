package com.example.startproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;
@EnableJpaRepositories("com.start.demo")
@SpringBootApplication(scanBasePackages = "com.start.demo")
@EntityScan(basePackages = "com.start.demo.Entities")
@RestController
public class StartProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartProjectApplication.class, args);
	}

}
