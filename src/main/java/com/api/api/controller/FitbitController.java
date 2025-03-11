package com.api.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FitBitDTO;
import com.api.api.service.FitbitService;

@RestController
@RequestMapping("/api/fitbit")
public class FitbitController {

    private FitbitService fitbitService;

    public FitbitController(FitbitService fitbitService){
        this.fitbitService = fitbitService;
    }

    //Endpoint para recuperar la info de sueño del user
    @GetMapping("/sleep")
    public ResponseEntity<FitBitDTO.SleepDTO> getSleepInfo(){
        //Recuperamos el DTO con la info del sueño del user llamando al servicio
        FitBitDTO.SleepDTO sleepDTO = fitbitService.getSleepInfo();
        return ResponseEntity.ok(sleepDTO);
    }

    //Endpoint para recuperar la info de comida del user
    @GetMapping("/food")
    public ResponseEntity<FitBitDTO.FoodDTO> getFoodInfo(){
        //Recuperamos el DTO con la info del sueño del user llamando al servicio
        FitBitDTO.FoodDTO foodDTO = fitbitService.getFoodInfo();
        return ResponseEntity.ok(foodDTO);
    }
    
}
