package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="*")
public class AdminController {

    @Autowired
    private AnalysisRepository analysisRepository;

    // NEW METHOD → fetch analysis only for logged in admin
    @GetMapping("/analysis")
    public List<Analysis> getAdminAnalysis(@RequestParam String adminEmail){

        // BACKEND SECURITY CHECK (ADDED)
        if(adminEmail == null || adminEmail.isEmpty()){
            throw new RuntimeException("Unauthorized access");
        }

        return analysisRepository.findByAdminEmail(adminEmail);

    }

}