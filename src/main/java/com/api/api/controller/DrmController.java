package com.api.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<> generateReportAndSaveAnswers(@PathVariable("userId") Long userId, @Valid @RequestBody DRMRequestDTO drmRequestDTO){


    }
    
}
