package com.api.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.SaveAnswersDrmAndGenerateReportDTO;
import com.api.api.DTO.FormRequestDTO.DRMRequestDTO;
import com.api.api.service.DrmService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class DrmController {

    private DrmService drmService;

    @Autowired
    public DrmController(DrmService drmService){
        this.drmService = drmService;
    }

    //Endpoint que se encargará de guadar la respuesta del usuario al cuestionario en la BD y generar el informe con la ayuda de la IA
    @PostMapping("/{userId}/drm")
    public ResponseEntity<SaveAnswersDrmAndGenerateReportDTO> generateReportAndSaveAnswers(@PathVariable("userId") Long userId, @Valid @RequestBody DRMRequestDTO drmRequestDTO){
        //llamamosa la función del service que se encargará de guardar la respuesta del usuario al cuestionario en la BD y llamar a la IA para generar el informe
        SaveAnswersDrmAndGenerateReportDTO response = drmService.generateReportAndSaveAnswers(userId, drmRequestDTO);
        return ResponseEntity.ok(response);
    }
    
}
