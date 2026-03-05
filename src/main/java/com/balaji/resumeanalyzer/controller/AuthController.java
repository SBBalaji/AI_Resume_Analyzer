package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.User;
import com.balaji.resumeanalyzer.model.LoginHistory;
import com.balaji.resumeanalyzer.repository.UserRepository;
import com.balaji.resumeanalyzer.repository.LoginHistoryRepository;
import com.balaji.resumeanalyzer.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Random;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private EmailService emailService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    /* ==============================
       SIGNUP API
       ============================== */

    @PostMapping("/signup")
    public String signup(@RequestBody User user){

        User existing = userRepository.findByEmail(user.getEmail());

        if(existing != null){
            return "Email already exists";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return "Signup Successful";
    }


    /* ==============================
       LOGIN API
       ============================== */

    @PostMapping("/login")
    public String login(@RequestBody User loginUser){

        User user = userRepository.findByEmail(loginUser.getEmail());

        if(user == null){
            return "Invalid email";
        }

        if(!encoder.matches(loginUser.getPassword(), user.getPassword())){
            return "Invalid password";
        }

        LoginHistory history = new LoginHistory();

        history.setEmail(user.getEmail());
        history.setLoginTime(LocalDateTime.now().toString());

        loginHistoryRepository.save(history);

        return "Login Successful";
    }


    /* ==============================
       FORGOT PASSWORD API
       ============================== */

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email){

        User user = userRepository.findByEmail(email);

        if(user == null){
            return "Email not found";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        System.out.println("Generated OTP: " + otp); // DEBUG

        user.setResetOtp(otp);

        user.setOtpExpiry(
                LocalDateTime.now().plusMinutes(5).toString()
        );

        userRepository.save(user);

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending email";
        }

        return "OTP sent to email";
    }


    /* ==============================
       RESET PASSWORD API
       ============================== */

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword){

        User user = userRepository.findByEmail(email);

        if(user == null){
            return "User not found";
        }

        if(user.getResetOtp() == null){
            return "OTP not generated";
        }

        if(!user.getResetOtp().equals(otp)){
            return "Invalid OTP";
        }

        LocalDateTime expiryTime = LocalDateTime.parse(user.getOtpExpiry());

        if(LocalDateTime.now().isAfter(expiryTime)){
            return "OTP expired";
        }

        user.setPassword(encoder.encode(newPassword));

        user.setResetOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful";
    }

}