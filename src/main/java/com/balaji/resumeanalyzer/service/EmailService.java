package com.balaji.resumeanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false) // 🔥 optional (prevents crash if not configured)
    private JavaMailSender mailSender;

    public void sendOtp(String email, String otp) {

        // ✅ ALWAYS print OTP (for debugging)
        System.out.println("=================================");
        System.out.println("OTP for " + email + " is: " + otp);
        System.out.println("=================================");

        // ✅ Try sending email
        try {

            // If mailSender not configured → skip email
            if (mailSender == null) {
                System.out.println("⚠ Email service not configured. OTP printed only.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("balajikumar12624@gmail.com");
            message.setTo(email);
            message.setSubject("AI Resume Analyzer - Password Reset OTP");

            message.setText(
                    "Hello,\n\n" +
                    "Your OTP for password reset is: " + otp + "\n\n" +
                    "This OTP will expire in 5 minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Regards,\n" +
                    "AI Resume Analyzer Team"
            );

            mailSender.send(message);

            System.out.println("✅ OTP email sent successfully!");

        } catch (Exception e) {

            // 🔥 fallback → don't break app
            System.out.println("❌ Email sending failed. OTP shown in console.");
            e.printStackTrace();
        }
    }
}