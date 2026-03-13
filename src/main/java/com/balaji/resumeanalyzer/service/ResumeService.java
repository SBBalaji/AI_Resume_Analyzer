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

    String[] lines = text.split("\\n");

    Set<String> invalid = new HashSet<>(Arrays.asList(
        "experience","work","education","summary","objective",
        "skills","project","profile","internship",
        "graduate","program","engineering","technology"
    ));

    for(int i=0;i<10 && i<lines.length;i++){

        String line = lines[i].trim();

        if(line.length() < 3 || line.length() > 30)
            continue;

        if(line.matches(".*\\d.*"))
            continue;

        String lower = line.toLowerCase();

        boolean skip = false;
        for(String word:invalid){
            if(lower.contains(word)){
                skip = true;
                break;
            }
        }

        if(skip)
            continue;

        if(line.matches("^[A-Za-z]{2,}(\\s[A-Za-z]{1,}){1,2}$")){
            return line;
        }
    }

    return "Undefined";
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

    Pattern p = Pattern.compile(
        "\\b(b\\.?\\s?e|b\\.?\\s?tech|b\\.?\\s?sc|b\\.?\\s?a|b\\.?\\s?com|bba|mba|mbbs|llb|m\\.?\\s?tech|m\\.?\\s?e|m\\.?\\s?sc|phd)\\b",
        Pattern.CASE_INSENSITIVE
    );

    Matcher m = p.matcher(text);

    if(m.find()){

        String d = m.group().toLowerCase().replace(" ","");

        if(d.contains("be"))
            return "B.E";

        if(d.contains("btech"))
            return "B.Tech";

        if(d.contains("bsc"))
            return "B.Sc";

        if(d.contains("ba"))
            return "B.A";

        if(d.contains("bcom"))
            return "B.Com";

        if(d.contains("bba"))
            return "BBA";

        if(d.contains("mba"))
            return "MBA";

        if(d.contains("mbbs"))
            return "MBBS";

        if(d.contains("llb"))
            return "LLB";

        if(d.contains("mtech"))
            return "M.Tech";

        if(d.contains("me"))
            return "M.E";

        if(d.contains("msc"))
            return "M.Sc";

        if(d.contains("phd"))
            return "PhD";
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
        "([A-Za-z .,&-]{5,120}(College|University|Institute|Technology))",
        Pattern.CASE_INSENSITIVE
    );

    Matcher m = p.matcher(text);

    while(m.find()){

        String uni = m.group().trim().toLowerCase();

        if(uni.contains("school") ||
           uni.contains("higher secondary") ||
           uni.contains("hr sec") ||
           uni.contains("matric") ||
           uni.contains("secondary"))
        {
            continue;
        }

        return capitalizeWords(m.group().trim());
    }

    return "Undefined";
}

/////////////////////////////////////////////////
//Capitalizewords
/////////////////////////////////////////////////

public String capitalizeWords(String str){

    String[] words = str.split(" ");
    StringBuilder result = new StringBuilder();

    for(String w : words){

        if(w.length() > 1)
            result.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
    }

    return result.toString().trim();
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

            if(experience != null && !experience.equals("0 Years"))
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