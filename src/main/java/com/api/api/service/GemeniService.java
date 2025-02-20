package com.api.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GemeniService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    public GemeniService(WebClient webClient){
        this.webClient = webClient;
    }

    //Creamos la función para mandar un mensaje a la api de gemini como prompt
    public String sendMessage(String message){
        String apiUrl = "/models/gemini-pro:generateText?key=" + apiKey;

        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of("prompt", Map.of("text",message)); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }
    
}
