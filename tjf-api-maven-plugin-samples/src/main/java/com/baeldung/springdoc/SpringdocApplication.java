package com.baeldung.springdoc;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

@SpringBootApplication
public class SpringdocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringdocApplication.class, args);
	}

	@Bean
	public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
		return openApi -> {

			openApi.getServers().forEach(server -> server.setUrl("{{host}}/api/v1"));
			
			Paths paths = new Paths();

			openApi.getPaths().forEach((String key, PathItem pathItem) -> {
				paths.addPathItem(key.substring(17), pathItem);
			});

			openApi.setPaths(paths);
		};
	}
}
