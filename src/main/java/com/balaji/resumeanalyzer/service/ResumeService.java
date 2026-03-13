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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.time.LocalDateTime;
import java.time.Year;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        StringBuilder ocrText = new StringBuilder();

        for(int i = 0; i < document.getNumberOfPages(); i++){

            BufferedImage image = renderer.renderImageWithDPI(i,300);
            ocrText.append(tesseract.doOCR(image));
        }

        text = ocrText.toString();
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
        "(\\+91[ -]?)?[6-9]\\d{4}[ -]?\\d{5}"
    );

    Matcher m = p.matcher(text);

    if(m.find())
        return m.group();

    return "Unknown";
}

////////////////////////////////////////////////////
// NAME
////////////////////////////////////////////////////

public String extractName(String text){

String[] lines = text.split("\n");

for(String line : lines){

line = line.trim();

if(line.length() < 40 &&
!line.toLowerCase().contains("resume") &&
!line.toLowerCase().contains("email") &&
!line.toLowerCase().contains("phone") &&
line.matches("^[A-Za-z .]{3,40}$"))
{
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

    Map<String,String> degrees = new LinkedHashMap<>();

    // Engineering
    degrees.put("b.tech","B.Tech");
    degrees.put("b.e","B.E");
    degrees.put("m.tech","M.Tech");
    degrees.put("m.e","M.E");

    // Arts & Science
    degrees.put("b.sc","B.Sc");
    degrees.put("m.sc","M.Sc");
    degrees.put("b.a","B.A");
    degrees.put("m.a","M.A");
    degrees.put("b.com","B.Com");
    degrees.put("m.com","M.Com");
    degrees.put("bba","BBA");
    degrees.put("mba","MBA");

    // Medical
    degrees.put("mbbs","MBBS");
    degrees.put("md","MD");
    degrees.put("ms","MS");
    degrees.put("bds","BDS");
    degrees.put("b.pharm","B.Pharm");
    degrees.put("m.pharm","M.Pharm");

    // Law
    degrees.put("llb","LLB");
    degrees.put("llm","LLM");

    // Others
    degrees.put("phd","PhD");
    degrees.put("diploma","Diploma");
    degrees.put("polytechnic","Polytechnic");
    degrees.put("iti","ITI");

    for(String key : degrees.keySet()){

        if(text.contains(key))
            return degrees.get(key);
    }

    return "Undefined";
}
////////////////////////////////////////////////////
// STREAM
////////////////////////////////////////////////////

public String extractStream(String text){

    text = text.toLowerCase();

    Map<String,String> streams = new LinkedHashMap<>();

    // Engineering streams
    streams.put("computer science","Computer Science");
    streams.put("information technology","Information Technology");
    streams.put("artificial intelligence","Artificial Intelligence");
    streams.put("data science","Data Science");
    streams.put("electronics","Electronics");
    streams.put("electrical","Electrical Engineering");
    streams.put("mechanical","Mechanical Engineering");
    streams.put("civil","Civil Engineering");

    // Science streams
    streams.put("physics","Physics");
    streams.put("chemistry","Chemistry");
    streams.put("mathematics","Mathematics");
    streams.put("biology","Biology");
    streams.put("biotechnology","Biotechnology");

    // Commerce
    streams.put("commerce","Commerce");
    streams.put("accounting","Accounting");
    streams.put("finance","Finance");

    // Medical
    streams.put("medicine","Medicine");
    streams.put("pharmacy","Pharmacy");

    // Law
    streams.put("law","Law");

    for(String key : streams.keySet()){

        if(text.contains(key))
            return streams.get(key);
    }

    return "Undefined";
}
////////////////////////////////////////////////////
// UNIVERSITY / COLLEGE
////////////////////////////////////////////////////

public String extractUniversity(String text){

    Pattern p = Pattern.compile(
        "([A-Za-z .,&-]{5,100}(College|University|Institute|School))",
        Pattern.CASE_INSENSITIVE
    );

    Matcher m = p.matcher(text);

    while(m.find()){

        String uni = m.group().trim();

        if(!uni.toLowerCase().contains("high school"))
            return uni;
    }

    return "Unknown";
}

////////////////////////////////////////////////////
// PASSOUT YEAR
////////////////////////////////////////////////////

public String extractPassoutYear(String text){

    Pattern p = Pattern.compile("(19|20)\\d{2}");
    Matcher m = p.matcher(text);

    int year = 0;

    while(m.find()){
        int y = Integer.parseInt(m.group());

        // Accept only realistic graduation years
        if(y >= 1990 && y <= Year.now().getValue() + 6){

            if(y > year)
                year = y;
        }
    }

    if(year == 0)
        return "Undefined";

    return String.valueOf(year);
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

        if(passoutYear.equals("Undefined"))
            return "Undefined";

        int year = Integer.parseInt(passoutYear);
        int current = Year.now().getValue();

        if(year > current)
            return "Currently Pursuing";

        if(year == current)
            return "Final Year";

        if(year < current){

            if(!experience.equals("0 Years"))
                return "Working Professional";

            return "Graduate";
        }

    }catch(Exception e){

        return "Undefined";
    }

    return "Undefined";
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