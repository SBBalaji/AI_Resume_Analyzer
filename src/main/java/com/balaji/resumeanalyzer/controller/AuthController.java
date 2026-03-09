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

    /* ==============================
       SIGNUP API
       ============================== */

    @PostMapping("/signup")
    public String signup(@RequestBody User user){

        try{

            User existing = userRepository.findByEmail(user.getEmail());

            if(existing != null){
                return "Email already exists";
            }

            user.setPassword(encoder.encode(user.getPassword()));
            user.setRole("USER");

            userRepository.save(user);

            return "Signup Successful";

        }catch(Exception e){

            e.printStackTrace();
            return "Error during signup";

        }
    }


    /* ==============================
       LOGIN API
       ============================== */

    @PostMapping("/login")
    public String login(@RequestBody User loginUser){

        try{

            User user = userRepository.findByEmail(loginUser.getEmail());

            if(user == null){
                return "Invalid email";
            }

            if(!encoder.matches(loginUser.getPassword(), user.getPassword())){
                return "Invalid password";
            }

            user.setLoginTime(LocalDateTime.now().toString());
            userRepository.save(user);

            return "Login Successful";

        }catch(Exception e){

            e.printStackTrace();
            return "Login error";

        }
    }


    /* ==============================
       FORGOT PASSWORD API
       ============================== */

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email){

        try{

            User user = userRepository.findByEmail(email);

            if(user == null){
                return "Email not found";
            }

            // Generate OTP
            String otp = String.valueOf(new Random().nextInt(900000) + 100000);

            // Remove previous OTP if exists
            PasswordReset existingReset = passwordResetRepository.findByEmail(email);

            if(existingReset != null){
                passwordResetRepository.delete(existingReset);
            }

            // Create new OTP record
            PasswordReset reset = new PasswordReset();

            reset.setEmail(email);
            reset.setOtp(otp);

            reset.setExpiryTime(
                    LocalDateTime.now().plusMinutes(5).toString()
            );

            passwordResetRepository.save(reset);

            // Send Email
            emailService.sendOtp(email, otp);

            return "OTP sent to email";

        }catch(Exception e){

            e.printStackTrace();
            return "Error sending OTP";

        }
    }


    /* ==============================
       RESET PASSWORD API
       ============================== */

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword){

        try{

            PasswordReset reset = passwordResetRepository.findByEmail(email);

            if(reset == null){
                return "OTP not generated";
            }

            if(!reset.getOtp().equals(otp)){
                return "Invalid OTP";
            }

            LocalDateTime expiryTime = LocalDateTime.parse(reset.getExpiryTime());

            if(LocalDateTime.now().isAfter(expiryTime)){
                return "OTP expired";
            }

            User user = userRepository.findByEmail(email);

            if(user == null){
                return "User not found";
            }

            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);

            // Delete OTP record after password reset
            passwordResetRepository.delete(reset);

            return "Password reset successful";

        }catch(Exception e){

            e.printStackTrace();
            return "Error resetting password";

        }
    }

}