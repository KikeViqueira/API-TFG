package com.api.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.model.Drm;
import com.api.api.model.DrmAnswer;
import com.api.api.repository.DrmAnswerRepository;

@Service
public class DrmAnswerService {

    @Autowired
    private DrmAnswerRepository drmAnswerRepository;

    //Función que se encargará especificamente de guardar las respuestas del usuario al cuestionario en la BD
    public List<DrmAnswer> saveAnswers(Drm drm ,HashMap<String, String> answers){
        List<DrmAnswer> drmAnswers = new ArrayList<>();
        //Recorremos el hashmap y vamos guardando las respuestas en la BD
        for (String key : answers.keySet()){
            DrmAnswer drmAnswer = new DrmAnswer();
            drmAnswer.setQuestion(key);
            drmAnswer.setAnswer(answers.get(key));
            //Asiganamos a que DRM pertenecen las respuestas que metamos en la bd
            drmAnswer.setDrm(drm); 
            drmAnswerRepository.save(drmAnswer);
            //Guardamos la respuesta en el array que vamos a devolver al DrmService
            drmAnswers.add(drmAnswer);
        }
        return drmAnswers;
    }
    
}
