package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.ResumeDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeDetails, Long> {

}