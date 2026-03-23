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
    // ✅ UPLOAD + ANALYZE
    ////////////////////////////////////////////////////
    @PostMapping("/upload")
    public Map<String, Object> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("skills") String skills
    ) throws Exception {

        System.out.println("✅ UPLOAD API HIT");

        return resumeService.analyzeResume(file, skills);
    }

    ////////////////////////////////////////////////////
    // ✅ DOWNLOAD
    ////////////////////////////////////////////////////
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {

        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .header("Content-Type", "application/pdf")
                .body(analysis.getResumeFile());
    }
}