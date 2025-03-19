package com.api.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.DrmObjectDTO;
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

    /*
     * Endpoint que se encargará de recuperar el cuestionario DRM que se ha hecho en el día de hoy o los que estén entre el rango de fechas especfícados
     * 
     * Recibimos en la url el parámetro del tipo de búsqueda a realizar si devolver todos los informes o solo el de hoy si existe,
     *  si no se recibe nada se buscará el cuestionario de hoy
     * /drm?period=historical por ejemplo o =daily y dependiendo de lo que se reciba ejecutamos una función u otra
     */

    @GetMapping("/{userId}/drm")
    public ResponseEntity<?> getDrm(@RequestParam(value = "period", defaultValue = "daily") String period, @PathVariable("userId") Long userId){
       //Dependiendo del parámetro que hemos recibido hacemos una funcionalidad u otra
       if ("historical".equalsIgnoreCase(period)){
            /*
             * llamamos a la función del service que se encargará de recuperar todos los cuestionarios DRM que ha hecho el user en la app
             * 
             * Devolvemos el report, hora en la que se hizo y la id para reenderizarlo en el frontEnd de manera correcta
             * */
            List<DrmObjectDTO> drms = drmService.getHistoricalDrm(userId);
            return ResponseEntity.ok(drms);
       }
       else if("daily".equalsIgnoreCase(period)){
            String report = drmService.getTodayDrm(userId);
            return ResponseEntity.ok(report);
       }
       else{
            //En caso de que period tenga otro valor devolvemos un error informativo
            return ResponseEntity.badRequest().body("El parámetro period no soporta este valor");
       }
    }
    
}
