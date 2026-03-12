package com.balaji.resumeanalyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name="analysis")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminEmail;

    private String candidateEmail;

    private String candidateName;

    private String location;

    private String degree;

    private String stream;

    private String passoutYear;

    private String experience;

    private String currentStatus;

    private int matchScore;

    private String analysisDate;

    @Column(length = 1000)
    private String matchedSkills;

    @Column(length = 1000)
    private String missingSkills;

    public Long getId() { return id; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    public String getPassoutYear() { return passoutYear; }
    public void setPassoutYear(String passoutYear) { this.passoutYear = passoutYear; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public String getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(String analysisDate) { this.analysisDate = analysisDate; }

    public String getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(String matchedSkills) { this.matchedSkills = matchedSkills; }

    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }
}