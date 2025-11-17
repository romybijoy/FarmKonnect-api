package com.fc.chatservice;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ChatServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
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
