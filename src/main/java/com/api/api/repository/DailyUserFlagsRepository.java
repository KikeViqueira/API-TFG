package com.api.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.api.model.DailyUserFlags;

public interface DailyUserFlagsRepository extends JpaRepository<DailyUserFlags, Long> {

    //Encontrar las banderas correspondientes al user recibiendo por parámetros el id del user
    List<DailyUserFlags> findByUser_Id(Long idUser);

}
