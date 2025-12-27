package com.fc.postservice;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for Post Service.
 */
@SpringBootApplication
@Slf4j
public class PostServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceApplication.class);

	/**
	 * Global ModelMapper bean used in DTO <-> Entity conversions.
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {

		log.info("Starting Post Service Application...");
		SpringApplication.run(PostServiceApplication.class, args);
		log.info("Post Service Started Successfully.");
	}

	/**
	 * Logs all mapped REST endpoints after application startup.
	 */
	@Bean
	public ApplicationRunner runner(ApplicationContext ctx) {
		return args -> {
			logger.info("Mapped Endpoints:");
			RequestMappingHandlerMapping handlerMapping = ctx.getBean(RequestMappingHandlerMapping.class);

			log.info("Registered HTTP Endpoints:");
			handlerMapping.getHandlerMethods().forEach((info, method) -> {
				log.info("➡ {} → {}", info.getMethodsCondition(), info.getPatternValues());
			});

			log.info("Total Endpoints Registered: {}", handlerMapping.getHandlerMethods().size());
		};
	}
}
