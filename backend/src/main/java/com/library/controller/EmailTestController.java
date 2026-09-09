package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
public class EmailTestController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping
    public String sendTestEmail(@RequestParam String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("BiblioTrack Test Email");
        message.setText("If you're reading this, your BiblioTrack email system is working correctly!");
        mailSender.send(message);
        return "Test email sent to " + to;
    }
}