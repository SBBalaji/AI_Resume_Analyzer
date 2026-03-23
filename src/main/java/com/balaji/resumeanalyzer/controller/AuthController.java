package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.User;
import com.balaji.resumeanalyzer.model.PasswordReset;
import com.balaji.resumeanalyzer.repository.UserRepository;
import com.balaji.resumeanalyzer.repository.PasswordResetRepository;
import com.balaji.resumeanalyzer.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Random;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    ////////////////////////////////////////////////////
    // ✅ SIGNUP (FULL FIXED)
    ////////////////////////////////////////////////////
    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody User user){

        Map<String, Object> response = new HashMap<>();

        try {

            if(user.getEmail() == null || user.getEmail().isEmpty()){
                response.put("success", false);
                response.put("message", "Email required");
                return response;
            }

            if(user.getPassword() == null || user.getPassword().length() < 5){
                response.put("success", false);
                response.put("message", "Password must be 5+ chars");
                return response;
            }

            if(user.getName() == null || user.getName().isEmpty()){
                response.put("success", false);
                response.put("message", "Name required");
                return response;
            }

            User existing = userRepository.findByEmail(user.getEmail());

            if(existing != null){
                response.put("success", false);
                response.put("message", "Email already exists");
                return response;
            }

            // 🔥 IMPORTANT FIX
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRole("USER");

            User savedUser = userRepository.save(user);

            System.out.println("✅ USER SAVED: " + savedUser.getEmail());

            response.put("success", true);
            response.put("name", savedUser.getName());
            response.put("email", savedUser.getEmail());

            return response;

        } catch (Exception e) {

            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            return response;
        }
    }

    ////////////////////////////////////////////////////
    // ✅ LOGIN
    ////////////////////////////////////////////////////
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser){

        Map<String, Object> response = new HashMap<>();

        try {

            User user = userRepository.findByEmail(loginUser.getEmail());

            if(user == null){
                response.put("success", false);
                response.put("message", "Invalid email");
                return response;
            }

            if(!encoder.matches(loginUser.getPassword(), user.getPassword())){
                response.put("success", false);
                response.put("message", "Invalid password");
                return response;
            }

            // SAVE LOGIN TIME
            user.setLoginTime(LocalDateTime.now().toString());
            userRepository.save(user);

            response.put("success", true);
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            return response;

        } catch (Exception e) {

            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Login error");
            return response;
        }
    }

    ////////////////////////////////////////////////////
    // ✅ FORGOT PASSWORD
    ////////////////////////////////////////////////////
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email){

        User user = userRepository.findByEmail(email);

        if(user == null){
            return "Email not found";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        PasswordReset reset = new PasswordReset();
        reset.setEmail(email);
        reset.setOtp(otp);
        reset.setExpiryTime(LocalDateTime.now().plusMinutes(5).toString());

        passwordResetRepository.save(reset);

        emailService.sendOtp(email, otp);

        return "OTP sent";
    }

    ////////////////////////////////////////////////////
    // ✅ RESET PASSWORD
    ////////////////////////////////////////////////////
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword){

        PasswordReset reset = passwordResetRepository.findByEmail(email);

        if(reset == null || !reset.getOtp().equals(otp)){
            return "Invalid OTP";
        }

        User user = userRepository.findByEmail(email);

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        passwordResetRepository.delete(reset);

        return "Password updated";
    }
}