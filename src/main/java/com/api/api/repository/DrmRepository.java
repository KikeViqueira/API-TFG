package com.api.api.repository;

import java.time.ZonedDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Drm;

@Repository
public interface DrmRepository extends JpaRepository<Drm, Long> {

    //Función que nos devolverá una bandera diciendo si el user ha hecho un cuestionario o no en el día que buscamos
    boolean existsByUser_IdAndTimeStampBetween(Long userId, ZonedDateTime startOfDay, ZonedDateTime endOfDay);
    
}
