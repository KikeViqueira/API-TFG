package com.api.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.api.model.ConfigurationUserFlags;

public interface ConfigurationUserFlagsRepository extends JpaRepository<ConfigurationUserFlags, Long> {

    //Encontrar las banderas correspondientes al user recibiendo por parámetros el id del user
    List<ConfigurationUserFlags> findByUser_Id(Long idUser);

}
