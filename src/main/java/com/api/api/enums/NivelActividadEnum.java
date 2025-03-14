package com.api.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NivelActividadEnum {
    MUY_BAJO("Muy bajo"),
    BAJO("Bajo"),
    MODERADO("Moderado"),
    ALTO("Alto"),
    MUY_ALTO("Muy Alto");

    private final String description;

    private NivelActividadEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static NivelActividadEnum fromDescription(String description) {
        for (NivelActividadEnum value : values()) {
            if (value.getDescription().equalsIgnoreCase(description)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Valor no válido para el nivel de actividad: " + description);
    }
}
