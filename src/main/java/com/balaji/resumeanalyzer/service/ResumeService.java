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
    // Extract Phone Number
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

        String[] lines = text.split("\n");

        if(lines.length > 0){
            return lines[0];
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

            if(university.contains("college") || university.contains("university")){
                return matcher.group().trim();
            }

            if(university.contains("institute")){
                return matcher.group().trim();
            }
        }

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

        if(text.contains("computer science") || text.contains("cse"))
            return "Computer Science";

        if(text.contains("information technology") || text.contains("it"))
            return "Information Technology";

        if(text.contains("electronics and communication") || text.contains("ece"))
            return "Electronics & Communication";

        if(text.contains("electrical") || text.contains("eee"))
            return "Electrical Engineering";

        if(text.contains("mechanical"))
            return "Mechanical Engineering";

        if(text.contains("civil"))
            return "Civil Engineering";

        if(text.contains("artificial intelligence"))
            return "Artificial Intelligence";

        if(text.contains("data science"))
            return "Data Science";

        return "Unknown";
    }

    // =====================================
    // Extract Passout Year
    // =====================================
    public String extractYear(String text){

        Pattern pattern = Pattern.compile("(19|20)\\d{2}");

        Matcher matcher = pattern.matcher(text);

        int latestYear = 0;

        while(matcher.find()){

            int year = Integer.parseInt(matcher.group());

            if(year > latestYear && year <= java.time.Year.now().getValue()){
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
    // Analyze Skills
    // =====================================
    public Map<String,Object> analyzeSkills(
            String resumeText,
            String jobDesc,
            String adminEmail,
            String resumeName){

        List<String> jobSkills = Arrays.asList(jobDesc.toLowerCase().split(","));

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

        int percentage = (matched.size()*100)/jobSkills.size();

        String passoutYear = extractYear(resumeText);
        String experience = extractExperience(resumeText);

        String status = "Not Defined";

        try{

            int currentYear = java.time.Year.now().getValue();
            int year = Integer.parseInt(passoutYear.replaceAll("[^0-9]",""));

            if(year > currentYear){
                status = "Studying";
            }
            else if(year == currentYear){
                status = "Final Year";
            }
            else{

                if(!experience.equals("0 Years")){
                    status = "Working Professional";
                }
                else{
                    status = "Completed College";
                }
            }

        }catch(Exception e){
            status = "Not Defined";
        }

        Analysis analysis = new Analysis();

        analysis.setAdminEmail(adminEmail);

        analysis.setCandidateEmail(extractEmail(resumeText));
        analysis.setCandidateName(extractName(resumeText));

        analysis.setLocation(extractLocation(resumeText));

        analysis.setDegree(extractDegree(resumeText));
        analysis.setStream(extractStream(resumeText));

        analysis.setPassoutYear(passoutYear);

        analysis.setExperience(experience);

        analysis.setCurrentStatus(status);

        analysis.setMatchScore(percentage);

        analysis.setMatchedSkills(String.join(",",matched));
        analysis.setMissingSkills(String.join(",",missing));

        analysis.setAnalysisDate(
                java.time.LocalDateTime.now().toString()
        );

        analysisRepository.save(analysis);

        Map<String,Object> result = new HashMap<>();

        result.put("candidateEmail",extractEmail(resumeText));
        result.put("candidateName",extractName(resumeText));
        result.put("location",extractLocation(resumeText));

        result.put("degree",extractDegree(resumeText));
        result.put("stream",extractStream(resumeText));

        result.put("university",extractUniversity(resumeText));

        result.put("passoutYear",passoutYear);

        result.put("experience",experience);

        result.put("currentStatus",status);

        result.put("phone",extractPhone(resumeText));

        result.put("matchScore",percentage);

        result.put("matchedSkills",matched);
        result.put("missingSkills",missing);

        return result;
    }
}