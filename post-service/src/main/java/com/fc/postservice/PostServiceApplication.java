package com.fc.postservice;

import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PostServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceApplication.class);

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(PostServiceApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(ApplicationContext ctx) {
		return args -> {
			logger.info("Mapped Endpoints:");
			ctx.getBean(RequestMappingHandlerMapping.class)
					.getHandlerMethods()
					.forEach((info, method) -> logger.info("{}", info));
		};
	}
}
