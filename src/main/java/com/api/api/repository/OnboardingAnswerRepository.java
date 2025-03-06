package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.OnboardingAnswer;

@Repository
public interface OnboardingAnswerRepository extends JpaRepository<OnboardingAnswer, Long>{

    /*
     * Función que devuelve un booleano en base a si existen o no respuestas para un cierto objeto Onboarding sabiendo su id 
     */
    boolean existsByOnboarding_Id(Long idOnboarding);
    
}
