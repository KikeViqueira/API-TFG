package com.api.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.model.Onboarding;
import com.api.api.model.OnboardingAnswer;
import com.api.api.repository.OnboardingAnswerRepository;


@Service
public class OnboardingAnswerService {

    @Autowired
    private OnboardingAnswerRepository onboardingAnswerRepository;

    //@Transactional NO HARIA FALTA USARLO SI SE LLAMA DESDE UNA FUNCION QUE YA ESTA ANOTADA CON @Transactional
    //Función que se encarga de guardar el mapa de respuestas en la BD (OnboardingAnswer)
    public List<OnboardingAnswer> saveOnboardingAnswers(HashMap<String, String> onboardingAnswers, Onboarding onboarding){
        List<OnboardingAnswer> onboardingAnswerList = new ArrayList<>();
        for (String key : onboardingAnswers.keySet()){
            OnboardingAnswer onboardingAnswer = new OnboardingAnswer();
            //Guardamos en el objeto a que Onboarding pertenece la respuesta
            onboardingAnswer.setOnboarding(onboarding);
            onboardingAnswer.setQuestion(key);
            onboardingAnswer.setAnswer(onboardingAnswers.get(key));
            /*
             * Una vez creado el objeto lo que tenemos que hacer es guardarlo en la BD, al ser la parte manejadora de la relación con guardar la respuesta ya se actualiza el objeto Onboarding
             * 
             * En caso de que la clave que hemos recuperado es la de la edad, no la guardamos en esta entidad poruqe ya la hemos guardado en la entidad user
             * No la eliminamos del mapa despues de guardarla en el OnboardingService ya que queremos devolver todas las respuestas que ha dado el user, en el DTO de la respuesta
            */
            if (!key.equals("question3")) onboardingAnswerRepository.save(onboardingAnswer);
            //Añadimos a la lista que se va a devolver el objeto que se acaba de guardar
            onboardingAnswerList.add(onboardingAnswer);
        }
        return onboardingAnswerList;
    }
    
}
