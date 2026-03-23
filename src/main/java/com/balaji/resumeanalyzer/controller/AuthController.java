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

    ////////////////////////////////////////////////////
    // SIGNUP
    ////////////////////////////////////////////////////

    @PostMapping("/signup")
    public String signup(@RequestBody User user){

        try {

            if(user.getEmail() == null || user.getEmail().isEmpty()){
                return "Email required";
            }

            if(user.getPassword() == null || user.getPassword().length() < 5){
                return "Password must be 5+ chars";
            }

            if(user.getName() == null || user.getName().isEmpty()){
                return "Name required";
            }

            User existing = userRepository.findByEmail(user.getEmail());

            if(existing != null){
                return "Email already exists";
            }

            user.setPassword(encoder.encode(user.getPassword()));
            user.setRole("USER");

            userRepository.save(user);

            // 🔥 RETURN USERNAME
            return user.getName();

        } catch (Exception e) {

            e.printStackTrace();
            return "Server error: " + e.getMessage();
        }
    }

    ////////////////////////////////////////////////////
    // LOGIN
    ////////////////////////////////////////////////////

    @PostMapping("/login")
    public String login(@RequestBody User loginUser){

        try {

            if(loginUser.getEmail() == null || loginUser.getEmail().isEmpty()){
                return "Email required";
            }

            if(loginUser.getPassword() == null || loginUser.getPassword().isEmpty()){
                return "Password required";
            }

            User user = userRepository.findByEmail(loginUser.getEmail());

            if(user == null){
                return "Invalid email";
            }

            if(!encoder.matches(loginUser.getPassword(), user.getPassword())){
                return "Invalid password";
            }

            // ✅ SAVE LOGIN TIME
            user.setLoginTime(LocalDateTime.now().toString());
            userRepository.save(user);

            return "Login Successful";

        } catch (Exception e) {

            e.printStackTrace();
            return "Login error: " + e.getMessage();
        }
    }

    ////////////////////////////////////////////////////
    // FORGOT PASSWORD (OTP)
    ////////////////////////////////////////////////////

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email){

        try {

            if(email == null || email.isEmpty()){
                return "Email is required";
            }

            User user = userRepository.findByEmail(email);

            if(user == null){
                return "Email not found";
            }

            // ✅ GENERATE OTP
            String otp = String.valueOf(new Random().nextInt(900000) + 100000);

            // ✅ DELETE OLD OTP
            PasswordReset existing = passwordResetRepository.findByEmail(email);

            if(existing != null){
                passwordResetRepository.delete(existing);
            }

            // ✅ SAVE NEW OTP
            PasswordReset reset = new PasswordReset();
            reset.setEmail(email);
            reset.setOtp(otp);
            reset.setExpiryTime(LocalDateTime.now().plusMinutes(5).toString());

            passwordResetRepository.save(reset);

            // ✅ SEND EMAIL
            emailService.sendOtp(email, otp);

            return "OTP sent to email";

        } catch (Exception e) {

            e.printStackTrace();
            return "Error sending OTP: " + e.getMessage();
        }
    }

    ////////////////////////////////////////////////////
    // RESET PASSWORD
    ////////////////////////////////////////////////////

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword){

        try {

            if(email == null || otp == null || newPassword == null){
                return "All fields are required";
            }

            PasswordReset reset = passwordResetRepository.findByEmail(email);

            if(reset == null){
                return "OTP not generated";
            }

            if(!reset.getOtp().equals(otp)){
                return "Invalid OTP";
            }

            LocalDateTime expiry = LocalDateTime.parse(reset.getExpiryTime());

            if(LocalDateTime.now().isAfter(expiry)){
                return "OTP expired";
            }

            User user = userRepository.findByEmail(email);

            if(user == null){
                return "User not found";
            }

            if(newPassword.length() < 5){
                return "Password must be at least 5 characters";
            }

            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);

            // ✅ DELETE OTP AFTER SUCCESS
            passwordResetRepository.delete(reset);

            return "Password reset successful";

        } catch (Exception e) {

            e.printStackTrace();
            return "Error resetting password: " + e.getMessage();
        }
    }
}