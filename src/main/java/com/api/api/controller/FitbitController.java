package com.api.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FitBitDTO.FoodDTO;
import com.api.api.DTO.FitBitDTO.SleepDTO;
import com.api.api.DTO.FitBitDTO.SleepWeeklyDTO;
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
    public ResponseEntity<SleepDTO> getSleepInfo(){
        SleepDTO sleepDTO = this.fitbitService.getSleepTodayInfo();
        return ResponseEntity.ok(sleepDTO);
    }

    //Endpoint para recuperar la info de comida del user
    @GetMapping("/food")
    public ResponseEntity<FoodDTO> getFoodInfo(){
        FoodDTO foodDTO = this.fitbitService.getFoodInfo();
        return ResponseEntity.ok(foodDTO);
    }

    //Endpoint para recuperar el registro semanal de sueño por parte del user
    @GetMapping("/sleepWeekly")
    public ResponseEntity<SleepWeeklyDTO> getSleepWeeklyInfo(){
        SleepWeeklyDTO sleepWeeklyDTO = this.fitbitService.getSleepWeeklyInfo();
        return ResponseEntity.ok(sleepWeeklyDTO);
    }
    
}
