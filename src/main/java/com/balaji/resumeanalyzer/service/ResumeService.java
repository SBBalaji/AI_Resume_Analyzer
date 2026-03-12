package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

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

//////////////////////////////////////////////////////
// EXTRACT TEXT FROM PDF
//////////////////////////////////////////////////////

public String extractText(MultipartFile file) throws Exception {

PDDocument document = PDDocument.load(file.getInputStream());
PDFTextStripper stripper = new PDFTextStripper();

String text = stripper.getText(document);

document.close();

return text;

}

//////////////////////////////////////////////////////
// EMAIL
//////////////////////////////////////////////////////

public String extractEmail(String text){

Pattern pattern =
Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");

Matcher matcher = pattern.matcher(text);

if(matcher.find()){
return matcher.group();
}

return "Not Found";

}

//////////////////////////////////////////////////////
// PHONE
//////////////////////////////////////////////////////

public String extractPhone(String text){

Pattern pattern =
Pattern.compile("(\\+91[- ]?)?[6-9]\\d{9}");

Matcher matcher = pattern.matcher(text);

if(matcher.find()){
return matcher.group();
}

return "Unknown";

}

//////////////////////////////////////////////////////
// NAME
//////////////////////////////////////////////////////

public String extractName(String text){

String[] lines = text.split("\n");

for(String line : lines){

line = line.trim();

if(line.length() < 30 &&
line.matches("[A-Z][a-z]+\\s[A-Z][a-zA-Z]*")){

if(!line.toLowerCase().contains("education")){
return line;
}

}

}

return "Unknown";

}

//////////////////////////////////////////////////////
// LOCATION
//////////////////////////////////////////////////////

public String extractLocation(String text){

text = text.toLowerCase();

if(text.contains("chennai")) return "Chennai";
if(text.contains("bangalore")) return "Bangalore";
if(text.contains("hyderabad")) return "Hyderabad";
if(text.contains("mumbai")) return "Mumbai";
if(text.contains("delhi")) return "Delhi";

return "Unknown";

}

//////////////////////////////////////////////////////
// DEGREE
//////////////////////////////////////////////////////

public String extractDegree(String text){

text = text.toLowerCase();

if(text.contains("b.tech")) return "B.Tech";
if(text.contains("b.e")) return "B.E";

return "Unknown";

}

//////////////////////////////////////////////////////
// STREAM
//////////////////////////////////////////////////////

public String extractStream(String text){

text = text.toLowerCase();

if(text.contains("artificial intelligence") &&
text.contains("data science"))
return "Artificial Intelligence and Data Science";

if(text.contains("computer science"))
return "Computer Science Engineering";

if(text.contains("information technology"))
return "Information Technology";

return "Unknown";

}

//////////////////////////////////////////////////////
// UNIVERSITY
//////////////////////////////////////////////////////

public String extractUniversity(String text){

Pattern pattern = Pattern.compile(
"([A-Z][A-Za-z .,&-]+(College|University|Institute))",
Pattern.CASE_INSENSITIVE
);

Matcher matcher = pattern.matcher(text);

while(matcher.find()){

String university = matcher.group();

if(!university.toLowerCase().contains("school")){
return university;
}

}

return "Unknown";

}

//////////////////////////////////////////////////////
// PASSOUT YEAR
//////////////////////////////////////////////////////

public String extractPassoutYear(String text){

Pattern pattern = Pattern.compile("(20\\d{2})");
Matcher matcher = pattern.matcher(text);

while(matcher.find()){

int year = Integer.parseInt(matcher.group());

if(year >= 2026 && year <= 2035){
return String.valueOf(year);
}

}

return "2027";

}

//////////////////////////////////////////////////////
// EXPERIENCE
//////////////////////////////////////////////////////

public String extractExperience(String text){

text = text.toLowerCase();

if(text.contains("intern")){
return "Internship";
}

Pattern pattern = Pattern.compile("(\\d+)\\s*(years|yrs)");
Matcher matcher = pattern.matcher(text);

if(matcher.find()){
return matcher.group();
}

return "0 Years";

}

//////////////////////////////////////////////////////
// STATUS
//////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////
// ANALYZE SKILLS
//////////////////////////////////////////////////////

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

if(resumeText.toLowerCase().contains(skill.trim()))
matched.add(skill.trim());
else
missing.add(skill.trim());

}

int score = (matched.size()*100)/jobSkills.size();

String passoutYear = extractPassoutYear(resumeText);
String experience = extractExperience(resumeText);
String status = calculateStatus(passoutYear,experience);

Analysis analysis = new Analysis();

analysis.setAdminEmail(adminEmail);
analysis.setCandidateEmail(extractEmail(resumeText));
analysis.setCandidateName(extractName(resumeText));
analysis.setPhone(extractPhone(resumeText));
analysis.setLocation(extractLocation(resumeText));
analysis.setDegree(extractDegree(resumeText));
analysis.setStream(extractStream(resumeText));
analysis.setUniversity(extractUniversity(resumeText));

analysis.setPassoutYear(passoutYear);
analysis.setExperience(experience);
analysis.setCurrentStatus(status);

analysis.setMatchScore(score);

analysis.setMatchedSkills(String.join(",",matched));
analysis.setMissingSkills(String.join(",",missing));

analysis.setAnalysisDate(
LocalDateTime.now().toString()
);

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