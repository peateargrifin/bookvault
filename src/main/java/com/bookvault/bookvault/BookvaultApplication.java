package com.bookvault.bookvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// 📘 CONCEPT: Video 14 - Enable @Scheduled annotation processing
public class BookvaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookvaultApplication.class, args);
	}

}
