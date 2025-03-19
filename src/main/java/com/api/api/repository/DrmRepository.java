package com.api.api.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Drm;

@Repository
public interface DrmRepository extends JpaRepository<Drm, Long> {

    //Función que nos devolverá una bandera diciendo si el user ha hecho un cuestionario o no en el día que buscamos
    boolean existsByUser_IdAndTimeStampBetween(Long userId, ZonedDateTime startOfDay, ZonedDateTime endOfDay);

    //hacemos la misma función que la anterior pero en este caso nos devolverá el objeto Drm si existe
    Optional<Drm> findByUser_IdAndTimeStampBetween(Long userId, ZonedDateTime startOfDay, ZonedDateTime endOfDay);

    //Creamos la función para recuperar todos los cuestionarios DRM que ha hecho el user en la app
    List<Drm> findAllByUser_Id(Long userId);
}
