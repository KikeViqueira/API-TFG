package com.api.api.controller;

import java.io.InputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FitbitTokenDTO;
import com.api.api.service.FitbitService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/fitbitAuth")
public class FitbitAuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FitbitService fitbitService;

    public FitbitAuthController(FitbitService fitbitService){
        this.fitbitService = fitbitService;
    }

    //Endpoint para que el user haga el login en Fitbit desde nuestra app
    @PostMapping("/login")
    public ResponseEntity<FitbitTokenDTO> login(@RequestBody Long userId){
        try (InputStream is = this.getClass().getResourceAsStream("/data/fitbitAuth.json")) {
            JsonNode node = this.objectMapper.readTree(is);
            //llamamos a la función del servicio que se encarga de guardar la info del login en Fitbit en nuestra BD
            FitbitTokenDTO fitbitTokenDTO = this.fitbitService.saveToken(node, userId);
            return ResponseEntity.ok(fitbitTokenDTO);
        } catch (Exception e) {
            throw new RuntimeException("No se ha podido encontrar el archivo: " + e.getMessage());
        }
    }
}
