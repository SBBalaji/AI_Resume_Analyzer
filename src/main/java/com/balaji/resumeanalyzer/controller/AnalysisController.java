package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired
    private AnalysisRepository analysisRepository;

    ////////////////////////////////////////////////////
    // ✅ USER ONLY DATA (IMPORTANT)
    ////////////////////////////////////////////////////
    @GetMapping("/user/{email}")
    public List<Analysis> getUserAnalysis(@PathVariable String email) {
        return analysisRepository.findByEmail(email);
    }
}