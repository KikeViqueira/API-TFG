package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.repository.TipRepository;

import jakarta.persistence.EntityNotFoundException;

import com.api.api.DTO.TipDTO;
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
    public List<TipDTO.TipResponseDTO> getTips(){
        //En caso de que no haya tips devolvemos null
        List<Tip> tips = tipRepository.findAll();
        if (tips.isEmpty()) throw new EntityNotFoundException("No hay tips en la BD");
        else{
            //Hacemos la conversión
            List<TipDTO.TipResponseDTO> tipsResponse = new ArrayList<>();
            for (Tip tip : tips) tipsResponse.add(new TipDTO.TipResponseDTO(tip));
            return tipsResponse;
        }
    }

    //Función para obtener un tip en concreto por su id en la BD
    public Tip geTipByID(Long id){
        return tipRepository.findById(id).orElse(null);
    }

    //Función para obtener un tip en concreto por su título en la BD //TODO: Esta función se usa para cuando se esta intentando crear una entidad llamando a un endpoint y no podemos usar ID ya que este se genera cuando la entidad se guarda en la BD
    public Tip geTipByTitle(String title){
        return tipRepository.findByTitle(title).orElse(null);
    }

    //Función para guardar un tip en la BD
    public TipDTO.TipResponseDTO createTip(Tip tip){
        //primero tenemos que comprobar que el tip que se está intentando crear no existe ya en la BD
        if (geTipByTitle(tip.getTitle()) != null){
            throw new IllegalArgumentException("El tip que se está intentando crear ya existe.");
        }else{
            Tip tipCreado = tipRepository.save(tip);
            return new TipDTO.TipResponseDTO(tipCreado);
        }
    }

    //Función para eliminar un tip de la BD
    public TipDTO.TipResponseDTO deleteTip(Long id){
        Tip tipRecuperado = geTipByID(id);
        if (tipRecuperado == null) throw new EntityNotFoundException("El tip que se está intentando eliminar no existe.");
        else  {
            tipRepository.deleteById(id);
            return new TipDTO.TipResponseDTO(tipRecuperado);
        }
    }

    //Función para recuperar la info detallada de un tip
    public TipDetail getDetailsTip(Long id){
        //Comprobamos si el tip que se ha seleccionado tiene detalles
        if (tipRepository.existsByIdAndTipDetailIsNotNull(id)) return tipRepository.findById(id).get().getTipDetail();
        else throw new EntityNotFoundException("El tip que se está intentando recuperar no tiene detalles.");
    }
}
