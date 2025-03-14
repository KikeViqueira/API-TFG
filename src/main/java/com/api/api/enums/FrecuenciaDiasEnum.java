package com.api.api.enums;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum FrecuenciaDiasEnum {
    NINGUNO("Ninguno"),
    ENTRE_1_Y_2_DIAS("Entre 1 y 2 días"),
    ENTRE_3_Y_4_DIAS("Entre 3 y 4 días"),
    ENTRE_5_Y_6_DIAS("Entre 5 y 6 días"),
    TODOS_LOS_DIAS("Todos los días");

    private final String description;

    private FrecuenciaDiasEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static FrecuenciaDiasEnum fromDescription(String description) {
        for (FrecuenciaDiasEnum value : values()) {
            if (value.getDescription().equalsIgnoreCase(description)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Valor no válido para la frecuencia de días: " + description);
    }
    
}
