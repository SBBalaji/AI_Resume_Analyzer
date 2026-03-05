package com.balaji.resumeanalyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name="login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String loginTime;

    public Long getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getLoginTime(){
        return loginTime;
    }

    public void setLoginTime(String loginTime){
        this.loginTime = loginTime;
    }
}