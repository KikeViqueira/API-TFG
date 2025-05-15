package com.api.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.api.model.DailyUserFlags;

public interface DailyUserFlagsRepository extends JpaRepository<DailyUserFlags, Long> {

    //Encontrar las banderas correspondientes al user recibiendo por parámetros el id del user y el día de hoy
    List<DailyUserFlags> findByUser_IdAndTimeStampBetween(Long idUser, LocalDateTime startOfDay, LocalDateTime endOfDay);

    //Función para recuperar la entidad que corresponde con la bandera diaria en caso de que exista o mandar null es caso contrario en el día de hoy
    Optional<DailyUserFlags> findByUser_IdAndFlagKeyAndTimeStampBetween(Long idUser, String flagKey, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
