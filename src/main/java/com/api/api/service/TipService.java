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
    public Tip geTipByID(Long id){
        return tipRepository.findById(id).orElse(null);
    }

    //Función para obtener un tip en concreto por su título en la BD //TODO: Esta función se usa para cuando se esta intentando crear una entidad llamando a un endpoint y no podemos usar ID ya que este se genera cuando la entidad se guarda en la BD
    public Tip geTipByTitle(String title){
        return tipRepository.findByTitle(title).orElse(null);
    }

    //Función para guardar un tip en la BD, //TODO: Los tips que se añaden en la bd a través de este endpoint tenemos que poner que son creados por el user que está logeado
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
