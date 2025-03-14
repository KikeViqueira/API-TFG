package com.api.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * La clase TiempoSueñoEnum define las opciones válidas para representar la duración del sueño
 * de un usuario. Cada constante del enum tiene una descripción asociada que se utiliza para mostrar
 * la opción de manera legible (por ejemplo, "Más de 8 horas").
 *
 * Además, esta clase es útil en el proceso de deserialización del JSON recibido desde el frontend.
 * Si el JSON envía el valor de la respuesta en base a la descripción (por ejemplo, "Más de 8 horas"),
 * se puede implementar un método estático o un deserializador personalizado que recorra las constantes
 * del enum y compare la descripción con el valor recibido. Esto asegura que únicamente se acepten
 * valores válidos, reforzando la integridad y consistencia de los datos.
 * 
 * Esto lo conseguimos con el método fromDescription, que recibe una descripción y devuelve la constante
 */
public enum TiempoSueñoEnum {
    MENOS_DE_5_HORAS("Menos de 5 horas"),
    ENTRE_5_Y_6_HORAS("Entre 5 y 6 horas"),
    ENTRE_6_Y_7_HORAS("Entre 6 y 7 horas"),
    ENTRE_7_Y_8_HORAS("Entre 7 y 8 horas"),
    MAS_DE_8_HORAS("Más de 8 horas");

    private final String description;

    //La única limitación que tienen los enumerados respecto a una clase normal es que si tiene constructor, este debe de ser privado para que no se puedan crear nuevos objetos.
    private TiempoSueñoEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /*
     * Cuando usamos un DTO con campos de tipo enum y aplicamos @Valid, Spring (mediante Jackson) se encarga de convertir los valores del JSON al enum.
     * Pero, por defecto, Jackson espera que el valor en el JSON coincida con el nombre de la constante (por ejemplo, "MENOS_DE_5_HORAS").

     * Si lo que enviamos es la descripción (por ejemplo, "Más de 8 horas"), debemos indicarle a Jackson cómo convertir ese valor a la constante correspondiente.
     * Esto se logra anotando el método estático (fromDescription) con @JsonCreator. De ese modo, Jackson llamará a ese método durante la deserialización
     * para que se haga la conversión basada en la descripción, y el DTO recibirá ya el enum correcto.
     */

     @JsonCreator
    public static TiempoSueñoEnum fromDescription(String description) {
        for (TiempoSueñoEnum tiempo : values()) {
            //Values() es un método que devuelve un array con todos los valores del enumerado
            if (tiempo.getDescription().equalsIgnoreCase(description)) {
                return tiempo;
            }
        }
        throw new IllegalArgumentException("Valor no válido para la pregunta sobre el tiempo de sueño " + description);
    }
}




