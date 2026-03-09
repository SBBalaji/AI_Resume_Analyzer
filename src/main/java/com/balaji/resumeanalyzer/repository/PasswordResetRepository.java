package com.balaji.resumeanalyzer.repository;

import com.balaji.resumeanalyzer.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    // Find OTP record by email
    PasswordReset findByEmail(String email);

    // Delete OTP record by email
    @Transactional
    void deleteByEmail(String email);

}