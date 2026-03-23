package com.jomap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;
@EnableJpaRepositories("com.jomap.backend")
@SpringBootApplication(scanBasePackages = "com.jomap.backend")
@EntityScan(basePackages = "com.jomap.backend.Entities")
@RestController
public class StartProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartProjectApplication.class, args);
	}

}
