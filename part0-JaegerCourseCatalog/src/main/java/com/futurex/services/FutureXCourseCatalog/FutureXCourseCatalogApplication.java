package com.futurex.services.FutureXCourseCatalog;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FutureXCourseCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutureXCourseCatalogApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(OpenTelemetry openTelemetry) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			openTelemetry.getPropagators().getTextMapPropagator().inject(
				Context.current(),
				request.getHeaders(),
				(carrier, key, value) -> carrier.set(key, value)
			);
			return execution.execute(request, body);
		});
		return restTemplate;
	}
}
