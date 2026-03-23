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
    // UPLOAD + ANALYZE
    ////////////////////////////////////////////////////

    @PostMapping("/upload")
    public Map<String, Object> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("skills") String skills
    ) throws Exception {

        // ✅ VALIDATION
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Resume file is required");
        }

        if (skills == null || skills.trim().isEmpty()) {
            throw new RuntimeException("Skills are required");
        }

        // ✅ CALL SERVICE
        return resumeService.analyzeResume(file, skills);
    }

    ////////////////////////////////////////////////////
    // DOWNLOAD RESUME
    ////////////////////////////////////////////////////

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {

        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .header("Content-Type", "application/pdf") // ✅ FIXED
                .body(analysis.getResumeFile());
    }
}