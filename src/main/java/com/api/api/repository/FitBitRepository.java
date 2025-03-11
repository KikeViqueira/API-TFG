package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.FitbitToken;

@Repository
public interface FitBitRepository extends JpaRepository<FitbitToken, Long> {
    
}
