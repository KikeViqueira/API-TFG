package com.api.api.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FormRequestDTO;
import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.service.OnboardingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private OnboardingService onboardingService;

    @Autowired
    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    //Definimos la ruta que se usará para guardar las respuesta al Onboarding por parte del user
    @PostMapping("/{userId}")
    public ResponseEntity<OnboardingAnswerDTO> saveOnboardingAnswers(@PathVariable("userId") Long userId, @RequestBody @Valid FormRequestDTO.OnboardingRequestDTO onboardingRequestDTO){ //Tenemos que poner @Valid para activar la validación del DTO
        //Llamamos a la función que se encarga de guardar las respuestas en la BD
        OnboardingAnswerDTO onboardingAnswerDTO = onboardingService.saveOnboardingAnswers(userId, onboardingRequestDTO.getData());
        return ResponseEntity.ok(onboardingAnswerDTO);
    } 

    //Endpoint para obtener las respuestas a preguntas con enunciado del Onboarding de un user, el de la edad no se recoge ya que no está guardado en la entidad OnboardingAnswer y tampoco es necesaria obtenerla aqui
    @GetMapping("/{userId}")
    public ResponseEntity<OnboardingAnswerDTO> getOnboardingAnswers(@PathVariable("userId") Long userId){
        //Llamamos a la función que se encarga de obtener las respuestas del Onboarding de un user
        OnboardingAnswerDTO onboardingAnswerDTO = onboardingService.getOnboardingAnswers(userId);
        return ResponseEntity.ok(onboardingAnswerDTO);
    }
    
}
