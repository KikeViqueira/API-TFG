package com.api.api.validation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/*
    * Esta clase implementa la lógica de validación para el Map de respuestas del onboarding.
    *
    * Define un conjunto fijo de claves permitidas (ALLOWED_KEYS) y, para cada clave, un conjunto de valores válidos (ALLOWED_VALUES).
    *
    * En el método isValid:
    *   - Se verifica que el mapa no sea nulo ni esté vacío.
    *   - Se itera sobre cada entrada del mapa:
    *       - Se comprueba que la clave esté en ALLOWED_KEYS.
    *       - Se comprueba que el valor asociado sea uno de los permitidos para esa clave (según ALLOWED_VALUES).
    *
    * Si alguna clave o valor no cumple lo establecido, se construye una violación de la validación con un mensaje personalizado,
    * y se retorna false. Si todas las entradas son válidas, se retorna true.
*/


public class DrmAnswersValidator implements ConstraintValidator<ValidOnboardingAnswers, Map<String, String>>{

    //Definimos los valores permitidos para las claves de cada par de respuesta recibidas por el user, la pregunta 6 no la tenemos que incluir ya que es opcional la definimos en un nuevo set
    private static final Set<String> MANDATORY_KEYS = Set.of("drm_question1", "drm_question2", "drm_question3", "drm_question4", "drm_question5");
    
    // Claves opcionales permitidas
    private static final Set<String> OPTIONAL_KEYS = Set.of("drm_question6");

    //Definimos los valores permitidos para las respuestas de cada pregunta
    private static final Map<String, Set<String>> ALLOWED_VALUES = Map.of(
    "drm_question2", Set.of("Mucho", "Algo", "Poco", "Nada"),
    "drm_question4", Set.of("Sí, con frecuencia", "A veces", "No, casi nunca"),
    "drm_question5", Set.of("Alegría", "Estrés", "Tristeza", "Neutral", "Otra")
);

    @Override
    public boolean isValid(Map<String, String> value, ConstraintValidatorContext context){
        //Comprobamos que el mapa que hemos recibido por parte del user no sea vacío
        if (value == null || value.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El mapa de respuestas no puede estar vacío")
                   .addConstraintViolation();
            return false;
        }

        //Comprobamos que se hayan enviado todas las claves obligatorias
        if (!value.keySet().containsAll(MANDATORY_KEYS)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El mapa de respuestas debe contener exactamente los siguientes campos: " + MANDATORY_KEYS)
                .addConstraintViolation();
            return false;
        }

        //Comprobamos que no se envíen claves no permitidas
        Set<String> allowedKeys = new HashSet<>();
        allowedKeys.addAll(MANDATORY_KEYS);
        allowedKeys.addAll(OPTIONAL_KEYS);
        //Recorremos las claves del mapa que el validator ha recibido
        for (String key : value.keySet()){
            if (!allowedKeys.contains(key)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Clave inválida: " + key)
                       .addConstraintViolation();
                return false;
            }
        }


        //Comprobamos que las claves y valores del mapa por cada entrada sean las permitidas
        for(Map.Entry<String, String> entry : value.entrySet()){
            String key = entry.getKey();
            String answer = entry.getValue();

            //Dependiendo de la key en la que esteamos hacemos las correspondientes validaciones
            switch (key) {
                case "drm_question1":
                case "drm_question3":
                    //En este caso lo que se espera es que la respuesta sea un número entero entre 1 y 10
                    int answerInt = Integer.parseInt(answer);
                    if (answerInt < 1 || answerInt > 10){
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("La respuesta para " + key + " debe ser un número entero entre 1 y 10")
                                .addConstraintViolation();
                        return false;
                    }
                    break;
                case "drm_question2":
                case "drm_question4":
                case "drm_question5":
                    //Comprobamos que el valor recibido para las preguntas correspondientes estén registradas en el mapa de respuestas permitidas
                    if (!ALLOWED_VALUES.get(key).contains(answer)){
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("La respuesta para " + key + " no es válida")
                                .addConstraintViolation();
                        return false;
                    }
                    break;
                // Para el comentario opcional se valida longitud y ausencia de contenido malicioso.
                case "drm_question6":
                    //Comprobamos si se ha mandado una respuesta no vacía
                    if (!answer.isEmpty()){
                        //Comprobamos que la longitud del comentario no sea mayor a 255 caracteres
                        if (answer.length() > 255){
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("El comentario no puede superar los 255 caracteres")
                                    .addConstraintViolation();
                            return false;
                        }
                        //verificamos de manera simple que el user no haya introducido en el contenido, texto en formato html
                        if (answer.matches(".*<[^>]+>.*")) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("El comentario contiene contenido malicioso")
                                   .addConstraintViolation();
                            return false;
                        }
                    }
                    break;
                default:
                    //Nunca se debería llegar aquí, ya que hemos comprobado que las claves sean válidas
                    break;
            }
        }
        return true;
    }
}
