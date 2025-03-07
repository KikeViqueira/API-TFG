package com.api.api.DTO;

import java.util.HashMap;

import com.api.api.validation.ValidSleepLogAnswers;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SleepLogRequestDTO {

    @NotEmpty(message = "El mapa de respuestas que tiene que mandar el user es obligatorio y no puede ser vacío")
    @ValidSleepLogAnswers //Validamos que las respuestas que se reciban sean correctas y sigan el formato que está definido, para así evitar errores en la BD e inyecciones de código 
    private HashMap<String, String> data;

    /*
     * Data es el hashmap que tenemos que pasar en la petición POST para guardar las respuestas del Onboarding, con el nombre de data 
     */
    
}
