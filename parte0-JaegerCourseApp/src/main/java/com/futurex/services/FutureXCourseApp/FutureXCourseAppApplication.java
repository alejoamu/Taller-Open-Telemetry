package com.futurex.services.FutureXCourseApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.opentelemetry.api.OpenTelemetry;

@SpringBootApplication
public class FutureXCourseAppApplication {

	public static void main(String[] args) {

		SpringApplication.run(FutureXCourseAppApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(OpenTelemetry openTelemetry) { 
		return new RestTemplate(); 
	}

}
