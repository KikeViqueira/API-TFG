package com.api.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.api.model.TipDetail;

public interface TipDetailRepository extends JpaRepository<TipDetail, Long>{

    //Tenemos que definir la función que en base a la ide de un tip devuelva su detalle
    TipDetail findByTipId(Long idTip);

    
}
