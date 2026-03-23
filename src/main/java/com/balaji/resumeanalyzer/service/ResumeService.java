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

        // OCR if text is too small
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

        // ✅ LIGHT CLEAN (keep structure)
        text = text.toLowerCase()
                   .replaceAll("\\r", " ")
                   .replaceAll("\\n", " ")
                   .replaceAll("\\t", " ");

        return text;
    }

    ////////////////////////////////////////////////////
    // EMAIL EXTRACTION
    ////////////////////////////////////////////////////

    public String extractEmail(String text) {
        Pattern p = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : "Not Found";
    }

    ////////////////////////////////////////////////////
    // MAIN ANALYSIS METHOD (FINAL FIXED)
    ////////////////////////////////////////////////////

    public Map<String, Object> analyzeResume(MultipartFile file, String skills) throws Exception {

        // 1. Extract text
        String text = extractText(file);

        // 🔥 STRONG NORMALIZATION (CRITICAL FIX)
        String normalizedText = text.toLowerCase()
                .replaceAll("[^a-z0-9]", "")   // remove symbols
                .replaceAll("\\s+", "");       // remove spaces

        System.out.println("TEXT LENGTH: " + text.length());
        System.out.println("NORMALIZED PREVIEW: " +
                normalizedText.substring(0, Math.min(200, normalizedText.length())));

        // 2. Skills list
        List<String> jobSkills = (skills == null || skills.isEmpty())
                ? new ArrayList<>()
                : Arrays.asList(skills.toLowerCase().split(","));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        int match = 0;

        // 3. MATCHING (STRONG)
        for (String skill : jobSkills) {

            String cleanSkill = skill.trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");

            if (normalizedText.contains(cleanSkill)) {
                matched.add(skill.trim());
                match++;
            } else {
                missing.add(skill.trim());
            }
        }

        System.out.println("MATCHED: " + matched);
        System.out.println("MISSING: " + missing);

        // 4. SCORE
        int score = jobSkills.size() == 0 ? 0 : (match * 100) / jobSkills.size();

        // 5. SAVE
        Analysis analysis = new Analysis();

        analysis.setEmail(extractEmail(text));
        analysis.setMatchScore(score);
        analysis.setMatchedSkills(String.join(",", matched));
        analysis.setMissingSkills(String.join(",", missing));
        analysis.setAnalysisDate(LocalDateTime.now().toString());
        analysis.setResumeFile(file.getBytes());

        analysisRepository.save(analysis);

        // 6. RESPONSE
        Map<String, Object> result = new HashMap<>();

        result.put("id", analysis.getId());
        result.put("email", analysis.getEmail());
        result.put("matchScore", score);
        result.put("matchedSkills", matched);
        result.put("missingSkills", missing);

        return result;
    }
}