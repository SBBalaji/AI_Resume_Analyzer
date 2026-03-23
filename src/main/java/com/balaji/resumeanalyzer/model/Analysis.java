package com.balaji.resumeanalyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "analysis")
public class Analysis {

    // ======================
    // PRIMARY KEY
    // ======================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ======================
    // MAIN REQUIRED FIELDS
    // ======================
    private String email;          // candidate email

    private int matchScore;        // score

    // ======================
    // RESUME FILE (DOWNLOAD)
    // 🔥 FIXED (VERY IMPORTANT)
    // ======================
    @Lob
    @Column(name = "resume_file", columnDefinition = "LONGBLOB")
    private byte[] resumeFile;

    // ======================
    // SKILLS
    // ======================
    @Column(length = 1000)
    private String matchedSkills;

    @Column(length = 1000)
    private String missingSkills;

    // ======================
    // DATE
    // ======================
    private String analysisDate;

    // ======================
    // GETTERS & SETTERS
    // ======================

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public byte[] getResumeFile() {
        return resumeFile;
    }

    public void setResumeFile(byte[] resumeFile) {
        this.resumeFile = resumeFile;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(String analysisDate) {
        this.analysisDate = analysisDate;
    }
}