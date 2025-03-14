package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.DrmAnswer;

@Repository
public interface DrmAnswerRepository extends JpaRepository<DrmAnswer, Long> {
    
}