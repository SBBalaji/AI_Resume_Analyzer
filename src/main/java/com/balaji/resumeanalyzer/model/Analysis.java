package com.balaji.resumeanalyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name="analysis")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminEmail;

    private String resumeEmail;

    private String resumeName;

    private int matchScore;

    private String analysisDate;

    @Column(length=1000)
    private String matchedSkills;

    @Column(length=1000)
    private String missingSkills;

    public Long getId(){ return id; }

    public String getAdminEmail(){ return adminEmail; }
    public void setAdminEmail(String adminEmail){ this.adminEmail = adminEmail; }

    public String getResumeEmail(){ return resumeEmail; }
    public void setResumeEmail(String resumeEmail){ this.resumeEmail = resumeEmail; }

    public String getResumeName(){ return resumeName; }
    public void setResumeName(String resumeName){ this.resumeName = resumeName; }

    public int getMatchScore(){ return matchScore; }
    public void setMatchScore(int matchScore){ this.matchScore = matchScore; }

    public String getAnalysisDate(){ return analysisDate; }
    public void setAnalysisDate(String analysisDate){ this.analysisDate = analysisDate; }

    public String getMatchedSkills(){ return matchedSkills; }
    public void setMatchedSkills(String matchedSkills){ this.matchedSkills = matchedSkills; }

    public String getMissingSkills(){ return missingSkills; }
    public void setMissingSkills(String missingSkills){ this.missingSkills = missingSkills; }
}