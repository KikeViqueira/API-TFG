package com.api.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FormRequestDTO;
import com.api.api.DTO.SleepLogAnswerDTO;
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
    public ResponseEntity<SleepLogAnswerDTO> createSleepLog(@PathVariable("userId") Long userId, @RequestBody @Valid FormRequestDTO.SleepLogRequestDTO sleepLogRequestDTO) {
        //Creamos el registro matutino del user una vez lo ha completado y enviado
        SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogService.createSleepLog(userId, sleepLogRequestDTO.getData());
        return ResponseEntity.ok(sleepLogAnswerDTO);
    }

    /*
     * Endpoint para recuperar la duración del sueño de un user durante los últimos 7 días, en principio
     * o Endpoint para recuperar la respuestas al cuestionario matutino de un user (ESTA PENSADO PARA ENSEÑAR SOLO LAS RESPUESTAS QUE SE HAN HECHO AL DÍA ACTUAL, Y MIENTRAS NO ACABE EL DÍA PODER VER QUE HAS RESPONDIDO)
     * El comportamiento del controller depende de que se reciba en los parámetros de la request
     * 
     * /sleep-logs?duration=7 -> Recuperar la duración del sueño de un user durante los últimos 7 días
     * /sleep-logs?duration=1 -> Recuperar las respuestas al cuestionario matutino de un user (solo se puede ver el día actual, no se pueden ver respuestas anteriores)
     * 
     */

    @GetMapping("/{userId}/sleep-logs")
    public ResponseEntity<?> getSleepLogsDuration (@RequestParam(value = "duration", defaultValue = "1") String duration,@PathVariable("userId") Long userId) {
        if ("7".equalsIgnoreCase(duration)){
            Map<String, Float> sleepLogsDuration = sleepLogService.getSleepLogsDuration(userId);
            return ResponseEntity.ok(sleepLogsDuration);

        }else if ("1".equalsIgnoreCase(duration)){
            SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogService.getSleepLog(userId);
            return ResponseEntity.ok(sleepLogAnswerDTO);
        }else{
            //En caso de que el valor recibido no sea 7 ni 1 devolvemos un error informativo
            return ResponseEntity.badRequest().body("El parámetro duration solo puede ser 7 o 1");
        }
        
    }
    
}
