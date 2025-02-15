package com.api.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.repository.TipRepository;
import com.api.api.model.Tip;
import com.api.api.model.TipDetail;

@Service
public class TipService {

    private TipRepository tipRepository;

    @Autowired
    public TipService(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }

    //Función para obtener todos los tips de la BD
    public List<Tip> getTips(){
        //En caso de que no haya tips devolvemos null
        return tipRepository.findAll();
    }

    //Función para obtener un tip en concreto por su id en la BD
    public Tip geTip(Long id){
        return tipRepository.findById(id).orElse(null);
    }

    //Función para guardar un tip en la BD
    public Tip createTip(Tip tip){
        return tipRepository.save(tip);
    }

    //Función para eliminar un tip de la BD
    public void deleteTip(Long id){
        tipRepository.deleteById(id);
    }

    //Función para recuperar la info detallada de un tip
    public TipDetail getDetailsTip(Long id){
        //Comprobamos si el tip que se ha seleccionado tiene detalles
        if (tipRepository.existsByIdAndTipDetailIsNotNull(id)) return tipRepository.findById(id).get().getTipDetail();
        return null;
    }
}
