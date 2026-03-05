package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.repository.AnalysisRepository;
import com.balaji.resumeanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    public long totalUsers() {
        return userRepository.count();
    }

    public long totalAnalysis() {
        return analysisRepository.count();
    }
}