package com.seaumsiddiqui.personalblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
@SpringBootApplication
public class PersonalblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalblogApplication.class, args);
	}

}
