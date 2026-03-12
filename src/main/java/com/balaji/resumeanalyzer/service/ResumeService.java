package com.balaji.resumeanalyzer.service;

import com.balaji.resumeanalyzer.model.Analysis;
import com.balaji.resumeanalyzer.repository.AnalysisRepository;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.time.LocalDateTime;
import java.time.Year;

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

if(text.trim().length() < 30){

PDFRenderer renderer = new PDFRenderer(document);
BufferedImage image = renderer.renderImageWithDPI(0, 300);

ITesseract tesseract = new Tesseract();
tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

text = tesseract.doOCR(image);

}

document.close();

return normalize(text);
}

////////////////////////////////////////////////////
// CLEAN TEXT
////////////////////////////////////////////////////

private String normalize(String text){

text = text.replaceAll("\\r", "\n");
text = text.replaceAll("\\t", " ");
text = text.replaceAll(" +", " ");

return text;
}

////////////////////////////////////////////////////
// EMAIL
////////////////////////////////////////////////////

public String extractEmail(String text){

Pattern p = Pattern.compile(
"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");

Matcher m = p.matcher(text);

if(m.find()) return m.group();

return "Not Found";
}

////////////////////////////////////////////////////
// PHONE
////////////////////////////////////////////////////

public String extractPhone(String text){

Pattern p = Pattern.compile(
"(\\+91[- ]?)?[6-9]\\d{9}");

Matcher m = p.matcher(text);

if(m.find()) return m.group();

return "Unknown";
}

////////////////////////////////////////////////////
// NAME
////////////////////////////////////////////////////

public String extractName(String text){

String[] lines = text.split("\n");

for(String line : lines){

line = line.trim();

if(line.length() < 35 &&
line.matches("[A-Z][a-zA-Z]+\\s[A-Z][a-zA-Z]+")){
return line;
}

}

return "Unknown";
}

////////////////////////////////////////////////////
// LOCATION
////////////////////////////////////////////////////

public String extractLocation(String text){

String[] cities = {
"chennai","coimbatore","madurai","salem",
"tirunelveli","erode","vellore","trichy"
};

text = text.toLowerCase();

for(String city : cities){

if(text.contains(city))
return city.substring(0,1).toUpperCase() + city.substring(1);

}

return "Unknown";
}

////////////////////////////////////////////////////
// DEGREE
////////////////////////////////////////////////////

public String extractDegree(String text){

text = text.toLowerCase();

if(text.contains("b.tech") || text.contains("btech"))
return "B.Tech";

if(text.contains("b.e") || text.contains("bachelor of engineering"))
return "B.E";

if(text.contains("m.tech"))
return "M.Tech";

return "Unknown";
}

////////////////////////////////////////////////////
// STREAM
////////////////////////////////////////////////////

public String extractStream(String text){

text = text.toLowerCase();

if(text.contains("computer science"))
return "Computer Science Engineering";

if(text.contains("artificial intelligence"))
return "Artificial Intelligence";

if(text.contains("data science"))
return "Data Science";

if(text.contains("information technology"))
return "Information Technology";

return "Unknown";
}

////////////////////////////////////////////////////
// UNIVERSITY / COLLEGE
////////////////////////////////////////////////////

public String extractUniversity(String text){

Pattern p = Pattern.compile(
"([A-Z][A-Za-z .,&-]+(College|University|Institute))",
Pattern.CASE_INSENSITIVE
);

Matcher m = p.matcher(text);

while(m.find()){

String uni = m.group();

if(!uni.toLowerCase().contains("school"))
return uni;

}

return "Unknown";
}

////////////////////////////////////////////////////
// PASSOUT YEAR
////////////////////////////////////////////////////

public String extractPassoutYear(String text){

Pattern p = Pattern.compile("(20\\d{2})");
Matcher m = p.matcher(text);

int latest = 0;

while(m.find()){

int y = Integer.parseInt(m.group());

if(y > latest)
latest = y;
}

if(latest != 0)
return String.valueOf(latest);

return "Not Defined";
}

////////////////////////////////////////////////////
// EXPERIENCE
////////////////////////////////////////////////////

public String extractExperience(String text){

text = text.toLowerCase();

if(text.contains("intern"))
return "Internship";

Pattern p = Pattern.compile("(\\d+)\\s*(years|yrs)");
Matcher m = p.matcher(text);

if(m.find())
return m.group();

return "0 Years";
}

////////////////////////////////////////////////////
// STATUS
////////////////////////////////////////////////////

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

return "Unknown";
}
}

////////////////////////////////////////////////////
// SKILL MATCHING
////////////////////////////////////////////////////

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