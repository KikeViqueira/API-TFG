package com.api.api.validation;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
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


public class OnboardingAnswersValidator implements ConstraintValidator<ValidOnboardingAnswers, Map<String, String>>{

    //Definimos los valores permitidos para las claves de cada par de respuesta recibidas por el user
    private static final Set<String> ALLOWED_KEYS = Set.of("question1", "question2", "question3", "question4", "question5");

    //Definimos los valores permitidos para las respuestas de cada pregunta
    private static final Map<String, Set<String>> ALLOWED_VALUES = Map.of(
    "question1", Set.of("Menos de 5 horas", "Entre 5 y 6 horas", "Entre 6 y 7 horas", "Entre 7 y 8 horas", "Más de 8 horas"),
    "question2", Set.of("Ninguno", "Entre 1 y 2 días", "Entre 3 y 4 días", "Entre 5 y 6 días", "Todos los días"),
    "question4", Set.of("Omnívora", "Vegetariana", "Vegana", "Flexitariana", "Otro"),
    "question5", Set.of("Muy bajo", "Bajo", "Moderado", "Alto", "Muy alto")
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

        //Comprobamos que las claves que se han recibido sean las permitidas y que el número sea el esperado
        if (value.size() != ALLOWED_KEYS.size() || !value.keySet().containsAll(ALLOWED_KEYS)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El mapa de respuestas debe contener exactamente los siguientes campos: " + ALLOWED_KEYS)
                .addConstraintViolation();
            return false;
        }

        //Comprobamos que las claves y valores del mapa por cada entrada sean las permitidas
        for(Map.Entry<String, String> entry : value.entrySet()){
            String key = entry.getKey();
            String answer = entry.getValue();

            System.out.println("obejto recibido: "+ value);

            //Si el valor de una de las claves que se esperan es nulo (String vacío) lanzamos error de validación
            if (answer == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("La respuesta para " + key + " no puede ser nula")
                       .addConstraintViolation();
                return false;
            }

            if (!ALLOWED_KEYS.contains(key)){ //Si la clave no está en las permitidas
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Clave inválida: " + key)
                       .addConstraintViolation();
                return false;
            }
             // Caso especial para question3 (edad)
            if ("question3".equals(key)) {
                   try {
                        //Asumimos que el formato que recibimos es el ISO
                        LocalDate fechaNacimiento = LocalDate.parse(answer);
                        LocalDate fechaActual = LocalDate.now();
                        int edad = Period.between(fechaNacimiento, fechaActual).getYears();
                        if (edad < 10 || edad > 100) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("La edad derivada de la fecha debe estar entre 10 y 100")
                                .addConstraintViolation();
                            return false;
                        }
                   } catch (DateTimeParseException e) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("La fecha de nacimiento no es válida. Formato esperado: YYYY-MM-DD")
                            .addConstraintViolation();
                        return false;
                   }
            }
            // Para las demás preguntas, comprobamos que el valor sea el permitido
            //Recuperamos los valores permitidos para la clave actual
            Set<String> allowed = ALLOWED_VALUES.get(key);
            if (allowed != null && !allowed.contains(answer)){ //Si el valor no está en los permitidos
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Respuesta inválida para " + key)
                       .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
