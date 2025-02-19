package com.api.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Tip;

@Repository
//El segundo parámetro de JpaRepository es el tipo de la clave primaria de la tabla
public interface TipRepository extends JpaRepository<Tip, Long>  {

    //Definimos la función para saber si el tip existe en la bd en llamadas post a la api
    Optional<Tip> findByTitle(String title); //Si queremos usar orElse tenemos que devolver en la función un Optional

    //Definimos la función que nos devolverá true o False dependiendo de si el tip pasado tiene TipDetails
    boolean existsByIdAndTipDetailIsNotNull(Long id);
}
