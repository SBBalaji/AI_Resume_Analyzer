package com.balaji.resumeanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String email, String otp) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            // Sender email
            message.setFrom("balajikumar12624@gmail.com");

            // Receiver email
            message.setTo(email);

            // Email subject
            message.setSubject("AI Resume Analyzer - Password Reset OTP");

            // Email body
            message.setText(
                    "Hello,\n\n" +
                    "Your OTP for password reset is: " + otp + "\n\n" +
                    "This OTP will expire in 5 minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Regards,\n" +
                    "AI Resume Analyzer Team"
            );

            // Send email
            mailSender.send(message);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Error sending OTP email");
        }
    }
}