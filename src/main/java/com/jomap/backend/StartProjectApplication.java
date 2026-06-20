package com.jomap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@EnableJpaRepositories("com.jomap.backend")
@SpringBootApplication(scanBasePackages = "com.jomap.backend")
@EntityScan(basePackages = "com.jomap.backend.Entities")
@RestController
@EnableScheduling
public class StartProjectApplication {

	  /*

        This is it. The final commit for JoMap and the official
        end of the graduation journey.

        To the future self opening this repo in years: Remember the late nights,
        the (kotlin & Spring Boot) errors , and
        the team( Ziad Qafisheh & Eyad Abufares & Ghaleb Shehab & Abdalqader Froukh & Mousab Makahleh ) 
		that built this from scratch.

        This project wouldn't be half as solid without Eyad’s absolute obsession with the details.
        Seriously, the man analyzes every single line of code, every pixel,
        and every edge case like his life depends on it.
        If this architecture holds up perfectly in 10 years,
        you can thank Eyad’s relentless 'just one more fix' attitude for it.

        Ziad Qafisheh & Iyad Abu Fares & Ghaleb Shehab & Abdalqader Froukh & Mousab Makahleh passed 
		through here.
		
        Closing this chapter. Project complete.


        */
	public static void main(String[] args) {
		SpringApplication.run(StartProjectApplication.class, args);
	}

}
