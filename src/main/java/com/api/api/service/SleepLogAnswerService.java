package com.api.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.SleepLogAnswerDTO;
import com.api.api.model.SleepLog;
import com.api.api.model.SleepLogAnswer;
import com.api.api.repository.SleepLogAnswerRepository;

import jakarta.transaction.Transactional;

@Service
public class SleepLogAnswerService {

    @Autowired
    private SleepLogAnswerRepository sleepLogAnswerRepository;

    //Función para guardar las respuestas del cuestionario matutino en la tabla de SleepLogAnswers
    @Transactional
    public SleepLogAnswerDTO saveAnswers(SleepLog sleepLog, HashMap<String, String> answers) {
        //Creamos el objeto respuesta que almacena la info recibida del cuestionario matutino
        SleepLogAnswer sleepLogAnswer = new SleepLogAnswer();
        sleepLogAnswer.setSleepTime(LocalDateTime.parse(answers.get("sleepTime")));
        sleepLogAnswer.setWakeUpTime(LocalDateTime.parse(answers.get("wakeUpTime")));
        sleepLogAnswer.setDuration(Float.parseFloat(answers.get("duration")));
        sleepLogAnswer.setAnswer1(answers.get("question1"));
        sleepLogAnswer.setAnswer2(answers.get("question2"));
        //Indicamos a que sleepLog pertenece el objeto respuesta
        sleepLogAnswer.setSleepLog(sleepLog);
        //Guardamos el objeto ken la BD
        sleepLogAnswerRepository.save(sleepLogAnswer);
        return new SleepLogAnswerDTO(sleepLogAnswer);
    }
    
}
