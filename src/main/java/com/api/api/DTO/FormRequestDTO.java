package com.api.api.DTO;

import java.util.HashMap;

import com.api.api.enums.DietaEnum;
import com.api.api.enums.FrecuenciaDiasEnum;
import com.api.api.enums.NivelActividadEnum;
import com.api.api.enums.TiempoSueñoEnum;
import com.api.api.validation.ValidDrmAnswers;
import com.api.api.validation.ValidOnboardingAnswers;
import com.api.api.validation.ValidSleepLogAnswers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


/**
 * 
 * Este DTO representará la estructura de pregunta respuesta de cualquier formulario que venga del frontEnd (Onboarding, SleepLog, DRM)


 * Tenemos que definir los posibles valores de los strings que se mandan en la solicitud a la api para cada una de las preguntas de respuesta de opción múltiple
 * 
 * Para la pregunta 1: {"Menos de 5 horas", "Entre 5 y 6 horas", "Entre 6 y 7 horas", "Entre 7 y 8 horas", "Más de 8 horas"}
 * Para la pregunta 2: {"Ninguno", "Entre 1 y 2 días", "Entre 3 y 4 días", "Entre 5 y 6 días", "Todos los días"}
 * Para la pregunta 4: {"Omnívora", "Vegetariana", "Vegana", "Flexitariana", "Otro"}
 * Para la pregunta 5: {"Muy bajo", "Bajo", "Moderado", "Alto", "Muy Alto"}
 * 
 * Usaremos enum es la mejor práctica porque define un conjunto fijo de constantes, garantizando seguridad de tipos, validación automática y claridad en el código,
 * lo que facilita la serialización y evita valores inesperados.
 */

public class FormRequestDTO {

    //lo que vamos a recibir del frontEnd es la siguiente estructura: {"question1": "Más de 8 horas", "question2": "Todos los días", "question3": 10, "question4": "Otro", "question5": "Muy alto"}

    @Getter @Setter
    public static class OnboardingRequestDTO {
        @NotEmpty(message = "Las respuestas no pueden estar vacías")
        @ValidOnboardingAnswers
        private HashMap<String, String> data;
    }

    @Getter @Setter
    public static class SleepLogRequestDTO {
        @NotEmpty(message = "Las respuestas no pueden estar vacías")
        @ValidSleepLogAnswers
        private HashMap<String, String> data;
    }

    @Getter @Setter
    public static class DRMRequestDTO {
        @NotEmpty(message = "Las respuestas no pueden estar vacías")
        @ValidDrmAnswers
        private HashMap<String, String> DrmAnswersUser;
    }
    

   /* @NotNull(message = "La respuesta a la pregunta 1 no puede estar vacía")
    private TiempoSueñoEnum AnswerQuestion1;

    @NotNull(message = "La respuesta a la pregunta 2 no puede estar vacía")
    private FrecuenciaDiasEnum AnswerQuestion2;

    @NotNull(message = "La respuesta a la pregunta 3 no puede estar vacía, ya que está reservada para la edad")
    @Min(10)
    @Max(100)
    private Integer AnswerAge; //Corresponde con la respuesta a la pregunta de la edad

    @NotNull(message = "La respuesta a la pregunta 4 no puede estar vacía")
    private DietaEnum AnswerQuestion4;

    @NotNull(message = "La respuesta a la pregunta 5 no puede estar vacía")
    private NivelActividadEnum AnswerQuestion5;*/
    
}

