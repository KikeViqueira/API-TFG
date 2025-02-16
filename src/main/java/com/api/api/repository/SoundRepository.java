package com.api.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Sound;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

    //Definimos la función para encontrar los sonidos en la BD que son estáticos gracias al atributo isDefault
    List<Sound> findByIsDefaultTrue(); //Con findBy indicamos que estamos buscando un campo y con IsDefaultTrue indicamos que queremos los sonidos que sean estáticos

    
} 
