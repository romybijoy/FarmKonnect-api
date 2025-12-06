package com.fc.authservice.config;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Email configuration class for setting up JavaMailSender.
 * This configuration reads SMTP settings from application properties
 * and prepares a mail sender instance for sending verification emails,
 * password reset emails, and other user-related notifications.
 * Sensitive mail credentials are injected from external configuration
 * and are never logged for security reasons.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@Configuration
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String mailHost;
    @Value("${spring.mail.port}")
    private String mailPort;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;

    /**
     * Creates and configures a JavaMailSender instance using SMTP settings.
     * TLS is enabled by default for secure email transmission.
     *
     * @return a fully configured JavaMailSender bean
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailHost);
        javaMailSender.setPort(Integer.parseInt(mailPort));
        javaMailSender.setUsername(mailUsername);
        javaMailSender.setPassword(mailPassword);

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.smtp.starttls.enable", "true");

        // SAFE log: no email credentials logged
        log.info("JavaMailSender bean initialized successfully");
        return javaMailSender;
    }
}