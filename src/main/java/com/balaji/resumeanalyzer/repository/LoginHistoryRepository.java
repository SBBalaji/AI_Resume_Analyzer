package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory,Long>{
}