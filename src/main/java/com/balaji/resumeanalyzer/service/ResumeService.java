package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.model.ResumeDetails;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;
import com.balaji.resumeanalyzer.repository.ResumeRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.regex.*;

@Service
public class ResumeService {

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    // =====================================
    // Extract Text from PDF
    // =====================================
    public String extractText(MultipartFile file) throws Exception {

        if (!file.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Only PDF files allowed");
        }

        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        return text;
    }

    // =====================================
    // Extract Email
    // =====================================
    public String extractEmail(String text){

        Pattern pattern = Pattern.compile(
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

        Matcher matcher = pattern.matcher(text);

        if(matcher.find()){
            return matcher.group();
        }

        return "Not Found";
    }

    // =====================================
    // Extract Phone
    // =====================================
    public String extractPhone(String text){

        Pattern pattern = Pattern.compile("(\\+91[- ]?)?[6-9]\\d{9}");

        Matcher matcher = pattern.matcher(text);

        if(matcher.find()){
            return matcher.group();
        }

        return "Unknown";
    }

    // =====================================
    // Extract Name
    // =====================================
    public String extractName(String text){

        Pattern pattern = Pattern.compile("([A-Z][a-z]+\\s[A-Z][a-z]+)");

        Matcher matcher = pattern.matcher(text);

        while(matcher.find()){

            String name = matcher.group();

            if(name.length() < 30){
                return name;
            }
        }

        String[] lines = text.split("\n");

        for(String line : lines){

            line = line.trim();

            if(line.length() < 40 &&
               line.matches("[A-Z][a-z]+\\s[A-Z][a-z]+.*")){
                return line;
            }
        }

        if(lines.length > 0){
            return lines[0].trim();
        }

        return "Unknown";
    }

    // =====================================
    // Extract Location
    // =====================================
    public String extractLocation(String text){

        text = text.toLowerCase();

        if(text.contains("chennai")) return "Chennai";
        if(text.contains("bangalore")) return "Bangalore";
        if(text.contains("hyderabad")) return "Hyderabad";
        if(text.contains("mumbai")) return "Mumbai";
        if(text.contains("delhi")) return "Delhi";
        if(text.contains("madurai")) return "Madurai";

        return "Unknown";
    }

    // =====================================
    // Extract Degree
    // =====================================
    public String extractDegree(String text){

        text = text.toLowerCase();

        if(text.contains("bachelor of engineering") || text.contains("b.e") || text.contains("be "))
            return "B.E";

        if(text.contains("bachelor of technology") || text.contains("b.tech") || text.contains("btech"))
            return "B.Tech";

        if(text.contains("master of engineering") || text.contains("m.e"))
            return "M.E";

        if(text.contains("master of technology") || text.contains("m.tech"))
            return "M.Tech";

        if(text.contains("bachelor of science") || text.contains("b.sc") || text.contains("bsc"))
            return "B.Sc";

        if(text.contains("master of science") || text.contains("m.sc") || text.contains("msc"))
            return "M.Sc";

        if(text.contains("bca"))
            return "BCA";

        if(text.contains("mca"))
            return "MCA";

        if(text.contains("mba"))
            return "MBA";

        return "Unknown";
    }

    // =====================================
    // Extract Stream
    // =====================================
    public String extractStream(String text){

        text = text.toLowerCase();

        if(text.contains("artificial intelligence") && text.contains("data science"))
            return "Artificial Intelligence and Data Science";

        if(text.contains("information technology"))
            return "Information Technology";

        if(text.contains("computer science"))
            return "Computer Science";

        if(text.contains("electronics"))
            return "Electronics";

        return "Unknown";
    }

    // =====================================
    // Extract University
    // =====================================
    public String extractUniversity(String text){

        Pattern pattern = Pattern.compile(
                "([A-Z][A-Za-z .,&-]+(College|University|Institute))",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        while(matcher.find()){

            String university = matcher.group().trim().toLowerCase();

            if(university.contains("school")){
                continue;
            }

            return matcher.group().trim();
        }

        return "Unknown";
    }

    // =====================================
    // Extract Passout Year
    // =====================================
    public String extractPassoutYear(String text){

        Pattern pattern = Pattern.compile("(19|20)\\d{2}");
        Matcher matcher = pattern.matcher(text);

        int latestYear = 0;

        while(matcher.find()){

            int year = Integer.parseInt(matcher.group());

            if(year > latestYear){
                latestYear = year;
            }
        }

        if(latestYear != 0){
            return String.valueOf(latestYear);
        }

        return "Not Defined";
    }

    // =====================================
    // Extract Experience
    // =====================================
    public String extractExperience(String text){

        Pattern pattern = Pattern.compile("(\\d+)\\+?\\s*(years|yrs)");
        Matcher matcher = pattern.matcher(text.toLowerCase());

        if(matcher.find()){
            return matcher.group();
        }

        return "0 Years";
    }

    // =====================================
    // Calculate Status
    // =====================================
    public String calculateStatus(String passoutYear,String experience){

        try{

            int currentYear = Year.now().getValue();
            int year = Integer.parseInt(passoutYear);

            if(year > currentYear)
                return "Studying";

            if(year == currentYear)
                return "Final Year";

            if(!experience.equals("0 Years"))
                return "Working Professional";

            return "Completed College";

        }catch(Exception e){

            return "Studying";
        }
    }

    // =====================================
    // Analyze Skills
    // =====================================
    public Map<String,Object> analyzeSkills(
            String resumeText,
            String jobDesc,
            String adminEmail,
            String resumeName){

        List<String> jobSkills =
                Arrays.asList(jobDesc.toLowerCase().split(","));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for(String skill : jobSkills){

            if(resumeText.toLowerCase().contains(skill.trim())){
                matched.add(skill.trim());
            }
            else{
                missing.add(skill.trim());
            }
        }

        int score = (matched.size()*100)/jobSkills.size();

        String passoutYear = extractPassoutYear(resumeText);
        String experience = extractExperience(resumeText);
        String status = calculateStatus(passoutYear,experience);

        // ===============================
        // Save Resume Details
        // ===============================
        ResumeDetails resume = new ResumeDetails();

        resume.setCandidateName(extractName(resumeText));
        resume.setEmail(extractEmail(resumeText));
        resume.setPhone(extractPhone(resumeText));
        resume.setLocation(extractLocation(resumeText));

        resume.setDegree(extractDegree(resumeText));
        resume.setStream(extractStream(resumeText));
        resume.setUniversity(extractUniversity(resumeText));

        resume.setPassoutYear(passoutYear);
        resume.setExperience(experience);
        resume.setCurrentStatus(status);

        resumeRepository.save(resume);

        // ===============================
        // Save Analysis
        // ===============================
        Analysis analysis = new Analysis();

        analysis.setAdminEmail(adminEmail);
        analysis.setCandidateEmail(resume.getEmail());
        analysis.setCandidateName(resume.getCandidateName());
        analysis.setPhone(resume.getPhone());
        analysis.setLocation(resume.getLocation());
        analysis.setDegree(resume.getDegree());
        analysis.setStream(resume.getStream());
        analysis.setUniversity(resume.getUniversity());
        analysis.setPassoutYear(passoutYear);
        analysis.setExperience(experience);
        analysis.setCurrentStatus(status);
        analysis.setMatchScore(score);
        analysis.setMatchedSkills(String.join(",",matched));
        analysis.setMissingSkills(String.join(",",missing));
        analysis.setAnalysisDate(LocalDateTime.now().toString());

        analysisRepository.save(analysis);

        Map<String,Object> result = new HashMap<>();
        result.put("candidateEmail",analysis.getCandidateEmail());
        result.put("candidateName",analysis.getCandidateName());
        result.put("phone",analysis.getPhone());
        result.put("location",analysis.getLocation());
        result.put("degree",analysis.getDegree());
        result.put("stream",analysis.getStream());
        result.put("university",analysis.getUniversity());
        result.put("passoutYear",analysis.getPassoutYear());
        result.put("experience",analysis.getExperience());
        result.put("currentStatus",analysis.getCurrentStatus());
        result.put("matchScore",analysis.getMatchScore());
        result.put("matchedSkills",matched);
        result.put("missingSkills",missing);

return result;
    }
}