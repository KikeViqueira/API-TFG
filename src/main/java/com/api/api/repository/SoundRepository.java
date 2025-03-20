package com.api.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Sound;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

    //TODO: Si creamos funciones las cuales consultan campos de una entidad el nombre de los atributos tenemos que ponerlo como en el modelo, no como en la bd

    //Definimos la función para encontrar los sonidos en la BD que son estáticos gracias al atributo isDefault
    List<Sound> findByIsDefaultTrue(); //Con findBy indicamos que estamos buscando un campo y con IsDefaultTrue indicamos que queremos los sonidos que sean estáticos

    //Definimos una funcion para devolver los sonidos los cuales tienen un determinado user relacionado
    List<Sound> findByOwnerId(Long userId); // Busca todos los sonidos donde owner.id = userId

    //Creamos una función para encontrar saber si un sonido ya esta relacionado con el user que llama a la función de subir un audio
    boolean existsByOwnerIdAndSource(Long idUser, String fileUrl); //Busca si existe un sonido con el owner.id = idUser y fileUrl = fileUrl

    
} 
