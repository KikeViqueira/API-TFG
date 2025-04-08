package com.api.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Tip;

@Repository
//El segundo parámetro de JpaRepository es el tipo de la clave primaria de la tabla
public interface TipRepository extends JpaRepository<Tip, Long>  {

    //Función para recuperar los tips de un determinado user en base a su id
    List<Tip> findByUser_Id(Long userId);

    //Función para determinar si existe una relación entre un tip y un user
    boolean existsByUser_IdAndId(Long userId, Long id);

    //Función para comprobar si un determinado tip es favorito o no
    boolean existsByIdAndIsFavoriteTrue(Long id);

    //Definimos la función para saber si el tip existe en la bd en llamadas post a la api
    Optional<Tip> findByTitle(String title); //Si queremos usar orElse tenemos que devolver en la función un Optional

    //Definimos la función que nos devolverá true o False dependiendo de si el tip pasado tiene TipDetails
    boolean existsByIdAndTipDetailIsNotNull(Long id);

    //Definimos la función para recuperar la lista de tips favoritos que tiene un user
    List<Tip> findByUser_IdAndIsFavoriteTrue(Long userId);

    //Tenemos que hacer una función que para recuperar los ids que pertenecen al user de una lista de ids dada
    List<Tip> findByUser_IdAndIdIn(Long userId, List<Long> ids);
}
