package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    // OPTIONAL: for sorting (useful for admin dashboard)
    List<Analysis> findAllByOrderByMatchScoreDesc();
}