package com.fc.postservice;

import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class PostServiceApplication {

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
			System.out.println("Mapped Endpoints:");
			ctx.getBean(RequestMappingHandlerMapping.class)
					.getHandlerMethods()
					.forEach((info, method) -> System.out.println(info));
		};
	}
}
