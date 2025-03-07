package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.SleepLogAnswer;

@Repository
public interface SleepLogAnswerRepository extends JpaRepository<SleepLogAnswer, Long>{

}
