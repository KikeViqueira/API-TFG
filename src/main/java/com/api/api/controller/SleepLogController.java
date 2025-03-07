package com.api.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.SleepLogAnswerDTO;
import com.api.api.DTO.SleepLogRequestDTO;
import com.api.api.service.SleepLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class SleepLogController {

    private SleepLogService sleepLogService;

    @Autowired
    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    //Endpoint para la creación de un nuevo registro de sueño
    @PostMapping("/{userId}/sleep-logs")
    //Recibimos el id del user y las respuestas de su cuestionario matutino
    public ResponseEntity<SleepLogAnswerDTO> createSleepLog(@PathVariable("userId") Long userId, @RequestBody @Valid SleepLogRequestDTO sleepLogRequestDTO) {
        //Creamos el registro matutino del user una vez lo ha completado y enviado
        SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogService.createSleepLog(userId, sleepLogRequestDTO.getData());
        return ResponseEntity.ok(sleepLogAnswerDTO);
    }

    //Endpoint para recuperar las respuestas al cuestionario matutino de un user (ESTA PENSADO PARA ENSEÑAR SOLO LAS RESPUESTAS QUE SE HAN HECHO AL DÍA ACTUAL, Y MIENTRAS NO ACABE EL DÍA PODER VER QUE HAS RESPONDIDO)
    @GetMapping("/{userId}/sleep-logs/{sleepLogId}")
    public ResponseEntity<SleepLogAnswerDTO> getSleepLog(@PathVariable("userId") Long userId, @PathVariable("sleepLogId") Long sleepLogId ){
        SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogService.getSleepLog(userId, sleepLogId);
        return ResponseEntity.ok(sleepLogAnswerDTO);
    }

    /*
     * Endpoint para recuperar la duración del sueño de un user durante los últimos 7 días, en principio
     * 
     * Para no devolver toda la info al user, tenemos que hacer un DTO que solo contenga la info que queremos devolver
     * en este caso solo duration de SleepLogAnswer
     */

    @GetMapping("/{userId}/sleep-logs")
    public ResponseEntity<Map<String, Float>> getSleepLogsDuration (@PathVariable("userId") Long userId) {
        Map<String, Float> sleepLogsDuration = sleepLogService.getSleepLogsDuration(userId);
        return ResponseEntity.ok(sleepLogsDuration);
    }
    
}
