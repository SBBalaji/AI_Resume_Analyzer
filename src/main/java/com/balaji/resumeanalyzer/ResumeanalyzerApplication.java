package com.balaji.resumeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.balaji.resumeanalyzer.model")
@EnableJpaRepositories("com.balaji.resumeanalyzer.repository")
public class ResumeAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeAnalyzerApplication.class, args);
    }
}