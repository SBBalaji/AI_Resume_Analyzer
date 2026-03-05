package com.balaji.resumeanalyzer.controller;

import com.balaji.resumeanalyzer.service.ResumeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins="*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public Map<String,Object> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDesc") String jobDesc,
            @RequestParam("adminEmail") String adminEmail
    ) throws Exception {

        // BACKEND SECURITY CHECK (ADDED)
        if(adminEmail == null || adminEmail.isEmpty()){
            throw new RuntimeException("Unauthorized access");
        }

        String resumeText = resumeService.extractText(file);

        return resumeService.analyzeSkills(
                resumeText,
                jobDesc,
                adminEmail,
                file.getOriginalFilename()
        );
    }

}