package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AnalysisRepository analysisRepository;

    ////////////////////////////////////////////////////
    // ✅ GET ALL ANALYSIS (MAIN ADMIN API)
    ////////////////////////////////////////////////////
    @GetMapping("/analysis")
    public List<Analysis> getAllAnalysis(
            @RequestParam(required = false) String adminEmail
    ) {
        // 🔥 Currently returning all (admin view)
        return analysisRepository.findAll();
    }

    ////////////////////////////////////////////////////
    // ✅ OPTIONAL: KEEP OLD ENDPOINT (BACKWARD SUPPORT)
    ////////////////////////////////////////////////////
    @GetMapping("/all")
    public List<Analysis> getAll() {
        return analysisRepository.findAll();
    }

    ////////////////////////////////////////////////////
    // ✅ NEW: FILTER BY EMAIL (ADMIN SEARCH)
    ////////////////////////////////////////////////////
    @GetMapping("/user/{email}")
    public List<Analysis> getByEmail(@PathVariable String email) {
        return analysisRepository.findByEmail(email);
    }

    ////////////////////////////////////////////////////
    // DELETE ANALYSIS
    ////////////////////////////////////////////////////
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        if (!analysisRepository.existsById(id)) {
            return "Analysis Not Found";
        }

        analysisRepository.deleteById(id);
        return "Deleted Successfully";
    }
}