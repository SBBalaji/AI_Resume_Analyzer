package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    // ✅ NEW (VERY IMPORTANT)
    List<Analysis> findByEmail(String email);

    // OPTIONAL
    List<Analysis> findAllByOrderByMatchScoreDesc();
}