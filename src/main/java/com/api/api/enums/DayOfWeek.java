package com.api.api.enums;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Enum que representa los días de la semana con sus nombres en español.
 * 
 * Proporciona métodos para:
 * - Convertir una instancia de java.time.DayOfWeek al enum propio.
 * - Obtener el nombre en español de un día dado un LocalDate.
 * - Convertir un mapa que tiene claves (en mayúsculas, en inglés) y valores numéricos, 
 *   a un mapa cuyos keys son los nombres en español formateados.
 * */

public enum DayOfWeek {
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    SATURDAY("Sábado"),
    SUNDAY("Domingo");

    private final String displayName;

    DayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Método estático para convertir java.time.DayOfWeek a nuestro enum
    public static DayOfWeek fromJavaDayOfWeek(java.time.DayOfWeek dayOfWeek) {
        return valueOf(dayOfWeek.name());
    }

    // Método para obtener el nombre en español desde un LocalDate
    public static String getSpanishNameFromDate(LocalDate date) {
        return fromJavaDayOfWeek(date.getDayOfWeek()).getDisplayName();
    }

    // Método estático para convertir todos los días de una semana
    public static Map<String, Float> convertWeekDays(Map<String, Float> originalMap) {
        Map<String, Float> convertedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : originalMap.entrySet()) {
            // Obtenemos el valor enum de DayOfWeek a partir del nombre (en mayúsculas)
            DayOfWeek day = DayOfWeek.valueOf(entry.getKey());
            convertedMap.put(day.getDisplayName(), entry.getValue());
        }
        return convertedMap;
    }
} 