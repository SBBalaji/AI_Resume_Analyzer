package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findAllByOrderByMatchScoreDesc();

    List<Analysis> findByAdminEmail(String adminEmail); // NEW

}