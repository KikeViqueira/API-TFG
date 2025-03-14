package com.api.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DietaEnum {
    OMNIVORA("Omnívora"),
    VEGETARIANA("Vegetariana"),
    VEGANA("Vegana"),
    FLEXITARIANA("Flexitariana"),
    OTRO("Otro");

    private final String description;

    private DietaEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static DietaEnum fromDescription(String description) {
        for (DietaEnum value : values()) {
            if (value.getDescription().equalsIgnoreCase(description)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Valor no válido para el tipo de dieta: " + description);
    }
}
