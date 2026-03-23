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
    // ✅ GET ALL ANALYSIS (MATCHES FRONTEND)
    ////////////////////////////////////////////////////
    @GetMapping("/analysis")
    public List<Analysis> getAllAnalysis(@RequestParam(required = false) String adminEmail) {

        // 🔥 For now returning all (you can filter later if needed)
        return analysisRepository.findAll();
    }

    ////////////////////////////////////////////////////
    // OPTIONAL: KEEP OLD ENDPOINT
    ////////////////////////////////////////////////////
    @GetMapping("/all")
    public List<Analysis> getAll() {
        return analysisRepository.findAll();
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