package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;
import com.balaji.resumeanalyzer.service.ResumeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AnalysisRepository analysisRepository;

    ////////////////////////////////////////////////////
    // ✅ UPLOAD + ANALYZE (FIXED WITH EMAIL)
    ////////////////////////////////////////////////////
    @PostMapping("/upload")
    public Map<String, Object> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("skills") String skills,
            @RequestParam("email") String email   // 🔥 IMPORTANT FIX
    ) throws Exception {

        System.out.println("✅ UPLOAD API HIT");

        // 🔥 FIXED METHOD CALL (3 PARAMS)
        return resumeService.analyzeResume(file, skills, email);
    }

    ////////////////////////////////////////////////////
    // ✅ DOWNLOAD RESUME
    ////////////////////////////////////////////////////
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {

        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume Not Found"));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .header("Content-Type", "application/pdf")
                .body(analysis.getResumeFile());
    }
}