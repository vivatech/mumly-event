package com.vivatech.mumly_event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MumlyEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(MumlyEventApplication.class, args);
	}

}
