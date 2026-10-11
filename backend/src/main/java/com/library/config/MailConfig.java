
package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(
            System.getenv().getOrDefault("SPRING_MAIL_HOST", "smtp.gmail.com")
        );
        mailSender.setPort(Integer.parseInt(
            System.getenv().getOrDefault("SPRING_MAIL_PORT", "587")
        ));
        mailSender.setUsername(System.getenv("SPRING_MAIL_USERNAME"));
        mailSender.setPassword(System.getenv("SPRING_MAIL_PASSWORD"));

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
