package com.api.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

public class SleepLogAnswersValidator implements ConstraintValidator<ValidSleepLogAnswers, Map<String, String>> {

    // Claves esperadas para el sleep log: campos de tiempo, duración y preguntas fijas.
    private static final Set<String> ALLOWED_KEYS = Set.of("sleepTime", "wakeUpTime", "duration", "question1", "question2");

    // Para las preguntas fijas definimos los valores permitidos.
    private static final Map<String, Set<String>> ALLOWED_VALUES = Map.of(
        "question1", Set.of("Muy buena", "Buena", "Regular", "Mala", "Muy mala"),
        "question2", Set.of("Muy descansado", "Descansado", "Ni descansado ni cansado", "Cansado", "Muy cansado")
    );

    // Claves de tiempo a validar en formato ISO-8601.
    private static final Set<String> TIME_KEYS = Set.of("sleepTime", "wakeTime");

    @Override
    public boolean isValid(Map<String, String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El mapa de respuestas no puede estar vacío")
                   .addConstraintViolation();
            return false;
        }

        // Validamos que el mapa tenga exactamente el número de campos esperados, para que se tenga que pasar en el body de la solicitud exactamente lo que se espera
        if (value.size() != ALLOWED_KEYS.size() || !value.keySet().containsAll(ALLOWED_KEYS)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("El mapa de respuestas debe contener exactamente los siguientes campos: " + ALLOWED_KEYS)
                .addConstraintViolation();
            return false;
        }

        String sleepTimeStr = null;
        String wakeTimeStr = null;

        for (Map.Entry<String, String> entry : value.entrySet()) {
            String key = entry.getKey();
            String answer = entry.getValue();

            // Si la clave es "duration", se valida que sea numérico y <= 86400000 ms (24 horas)
            if ("duration".equals(key)) {
                long durationValue = Long.parseLong(answer);
                if (durationValue > 86400000L) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("La duración no puede superar las 24 horas")
                            .addConstraintViolation();
                    return false;
                }
            }

            // Si es un campo de tiempo, validar el formato
            if (TIME_KEYS.contains(key)) {
                try {
                    LocalDateTime.parse(answer, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("El formato de " + key + " es inválido; se requiere ISO-8601")
                           .addConstraintViolation();
                    return false;
                }
                if ("sleepTime".equals(key)) {
                    sleepTimeStr = answer;
                } else if ("wakeTime".equals(key)) {
                    wakeTimeStr = answer;
                }
                continue;
            }

            // Para las preguntas fijas, comprueba que la clave esté permitida
            if (!ALLOWED_KEYS.contains(key)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Clave inválida: " + key)
                       .addConstraintViolation();
                return false;
            }
            // Comprueba que el valor esté entre las opciones permitidas para esa pregunta
            Set<String> allowed = ALLOWED_VALUES.get(key);
            if (allowed != null && !allowed.contains(answer)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Respuesta inválida para " + key)
                       .addConstraintViolation();
                return false;
            }
        }

        // Si se recibieron ambos campos de tiempo, se verifica que wakeTime sea posterior a sleepTime y que la diferencia no supere 24 horas.
        if (sleepTimeStr != null && wakeTimeStr != null) {
            try {
                LocalDateTime sleepTime = LocalDateTime.parse(sleepTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime wakeTime = LocalDateTime.parse(wakeTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (!wakeTime.isAfter(sleepTime)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("La hora de despertar debe ser posterior a la hora de dormir")
                           .addConstraintViolation();
                    return false;
                }
                long diffMillis = java.time.Duration.between(sleepTime, wakeTime).toMillis();
                if (diffMillis > 86400000L) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("La diferencia entre la hora de dormir y despertar no puede superar las 24 horas")
                           .addConstraintViolation();
                    return false;
                }
            } catch (DateTimeParseException e) {
                return false;
            }
        }

        return true;
    }
}
