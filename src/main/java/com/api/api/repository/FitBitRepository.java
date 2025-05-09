package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.FitbitToken;

@Repository
public interface FitBitRepository extends JpaRepository<FitbitToken, Long> {

    //Función para saber si el user tiene un token guardado en la BD
    Optional<FitbitToken> findByUser_Id(Long idUser);
    
}
