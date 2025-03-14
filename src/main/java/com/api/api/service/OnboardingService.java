package com.api.api.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.model.Onboarding;
import com.api.api.model.OnboardingAnswer;
import com.api.api.model.User;
import com.api.api.repository.OnboardingAnswerRepository;
import com.api.api.repository.OnboardingRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class OnboardingService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnboardingAnswerService onboardingAnswerService;

    @Autowired
    private OnboardingAnswerRepository onboardingAnswerRepository;


    @Transactional
    //Función que se encarga de guardar la respuesta a el Onboarding por parte del usuario en la BD
    public OnboardingAnswerDTO saveOnboardingAnswers (Long userId, HashMap<String, String> answers){
        //Lo primero que tenemos que hacer es comprobar que el user exista en la BD
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //recuperamos el Onboarding en base al id del user
        Onboarding onboarding = onboardingRepository.findByUser_Id(userId).orElseThrow(() -> new EntityNotFoundException("No se ha encontrado el Onboarding correspondiente al user"));
        //Comprobamos si el user ya ha realizado el Onboarding antes o si esta vacío
        boolean completed = onboardingAnswerRepository.existsByOnboarding_Id(onboarding.getId());

        if (!completed){
            /**
             * Si no lo ha completado delegamos la lógica en el servicio de respuestas para que las guarde en la BD de una manera correcta
             * 
             * Este servicio solo guardará las respuestas String que necesitamos guardar en identificador de la pregunta para entender el contexto de la respuesta
             * En el caso de Edad, es mejor crear un atributo en el objeto User para guardar la edad y no guardarla en las respuestas del Onboarding, ya que se entiende por el propio valor que es la edad
             * */
            user.setAge(Integer.parseInt(answers.get("question3")));
            List<OnboardingAnswer> onboardingAnswersList = onboardingAnswerService.saveOnboardingAnswers(answers, onboarding);
            //Una vez recuperamos el array de respuestas con el obejto ya bien creado lo parseamos al tipo correspondiente para devolver solo la info que interesa
            return new OnboardingAnswerDTO(onboardingAnswersList);
        }else throw new RelationshipAlreadyExistsException("El usuario ya ha realizado el Onboarding");
    }

    @Transactional
    //Fución que se encarga de obtener las respuestas del Onboarding de un user
    public OnboardingAnswerDTO getOnboardingAnswers(Long userId){
        //Primero comprobamos que el user exista
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Comprobamos si el user ha realizado el Onboarding
        Onboarding onboarding = onboardingRepository.findByUser_Id(userId).orElseThrow(() -> new EntityNotFoundException("No se ha encontrado el Onboarding correspondiente al user"));
        if (!onboarding.getAnswers().isEmpty()){
            //En caso de que exista devolvemos las respuestas
            //Pasamos al user solo la info necesaria y la sacamos del objeto onboarding que ya tiene la lista de respuestas
            return new OnboardingAnswerDTO(onboarding.getAnswers());
        }else throw new NoContentException("El usuario no ha realizado el Onboarding");
    }
}
