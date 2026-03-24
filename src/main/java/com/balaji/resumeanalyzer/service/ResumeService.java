package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.*;

@Service
public class ResumeService {

    @Autowired
    private AnalysisRepository analysisRepository;

    ////////////////////////////////////////////////////
    // TEXT EXTRACTION (PDF + OCR)
    ////////////////////////////////////////////////////
    public String extractText(MultipartFile file) throws Exception {

        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();

        String text = stripper.getText(document);

        // 🔥 OCR fallback
        if (text == null || text.trim().length() < 30) {

            PDFRenderer renderer = new PDFRenderer(document);
            ITesseract tesseract = new Tesseract();

            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

            StringBuilder ocrText = new StringBuilder();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                ocrText.append(tesseract.doOCR(image));
            }

            text = ocrText.toString();
        }

        document.close();

        // 🔥 CLEAN TEXT
        return text.toLowerCase()
                   .replaceAll("\\r", " ")
                   .replaceAll("\\n", " ")
                   .replaceAll("\\t", " ")
                   .trim();
    }

    ////////////////////////////////////////////////////
    // MAIN ANALYSIS METHOD
    ////////////////////////////////////////////////////
    public Map<String, Object> analyzeResume(
            MultipartFile file,
            String skills,
            String userEmail   // ✅ USER EMAIL
    ) throws Exception {

        // 1️⃣ Extract text
        String text = extractText(file);

        String normalizedText = text
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        // 2️⃣ Handle skills safely
        List<String> jobSkills = new ArrayList<>();

        if (skills != null && !skills.trim().isEmpty()) {
            jobSkills = Arrays.asList(skills.toLowerCase().split(","));
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        int match = 0;

        // 3️⃣ Compare skills
        for (String skill : jobSkills) {

            String cleanSkill = skill.trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");

            if (!cleanSkill.isEmpty() && normalizedText.contains(cleanSkill)) {
                matched.add(skill.trim());
                match++;
            } else {
                missing.add(skill.trim());
            }
        }

        // 4️⃣ Score
        int score = jobSkills.size() == 0 ? 0 : (match * 100) / jobSkills.size();

        ////////////////////////////////////////////////////
        // 🔥 SAVE DATA (USER BASED)
        ////////////////////////////////////////////////////
        Analysis analysis = new Analysis();

        analysis.setEmail(userEmail);   // ✅ IMPORTANT FIX
        analysis.setMatchScore(score);
        analysis.setMatchedSkills(String.join(",", matched));
        analysis.setMissingSkills(String.join(",", missing));
        analysis.setAnalysisDate(LocalDateTime.now().toString());
        analysis.setResumeFile(file.getBytes());

        analysisRepository.save(analysis);

        ////////////////////////////////////////////////////
        // RESPONSE
        ////////////////////////////////////////////////////
        Map<String, Object> result = new HashMap<>();

        result.put("id", analysis.getId());
        result.put("email", analysis.getEmail());
        result.put("matchScore", score);
        result.put("matchedSkills", matched);
        result.put("missingSkills", missing);

        return result;
    }
}