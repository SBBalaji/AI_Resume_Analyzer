package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.*;

@Service
public class ResumeService {

    @Autowired
    private AnalysisRepository analysisRepository;

    // Extract text from PDF
    public String extractText(MultipartFile file) throws Exception {

        // FILE TYPE VALIDATION
        if (!file.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Only PDF files allowed");
        }

        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();

        String text = stripper.getText(document);

        document.close();

        return text;
    }

    // Extract email from resume text
    public String extractEmail(String text) {

        Pattern pattern = Pattern.compile(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return "not_found";
    }

    public Map<String, Object> analyzeSkills(
            String resumeText,
            String jobDesc,
            String adminEmail,
            String resumeName) {

        List<String> jobSkills = Arrays.asList(jobDesc.toLowerCase().split(","));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : jobSkills) {

            if (resumeText.toLowerCase().contains(skill.trim())) {
                matched.add(skill.trim());
            } else {
                missing.add(skill.trim());
            }

        }

        int percentage = (matched.size() * 100) / jobSkills.size();

        Analysis analysis = new Analysis();

        analysis.setAdminEmail(adminEmail);

        // ✅ CORRECT EMAIL EXTRACTION FROM RESUME TEXT
        String resumeEmail = extractEmail(resumeText);
        analysis.setResumeEmail(resumeEmail);

        analysis.setResumeName(resumeName);

        analysis.setMatchScore(percentage);

        analysis.setMatchedSkills(String.join(",", matched));
        analysis.setMissingSkills(String.join(",", missing));

        String date = java.time.LocalDateTime.now().toString();
        analysis.setAnalysisDate(date);

        analysisRepository.save(analysis);

        Map<String, Object> result = new HashMap<>();

        result.put("resumeEmail", resumeEmail);
        result.put("matchScore", percentage);
        result.put("matchedSkills", matched);
        result.put("missingSkills", missing);

        return result;
    }
}