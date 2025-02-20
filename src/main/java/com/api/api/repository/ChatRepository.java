package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
}
