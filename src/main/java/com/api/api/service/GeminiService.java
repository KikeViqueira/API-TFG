package com.api.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    public GeminiService(WebClient webClient){
        this.webClient = webClient;
    }

    //Creamos la función para mandar un mensaje a la api de gemini como prompt
    public String sendMessage(String message){ 
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = "Responde a lo que te dice el user siempre basándote en teorías de la psicología de manera clara (si lo que te ha dicho tiene que ver con sus sueños o hábitos), en el resto de casos contesta brevemente que no estas diseñado para responder temas distintos. Quiero que respondas al user de manera profesional pero cercana y respuestas medias/cortas como si fuese una conversación de Whatsapp pero en detalle pero yendo al grano y sin expandirte demasiado pero sin ser cortante.";

        String fullPrompt = message + "\n\n" + prompt;


        //Construímos el cuerpo de la petición
        /*
        "contents": [{
        "parts":[{"text": "Explain how AI works"}]
        }]
    */
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }


    //FUNCION PARA CREAR UN TÍTULO EN BASE AL CONTENIDO DEL MENSAJE
    public String createTitle(String message){ 
        String apiUrl = "/models/gemini-1.5-flash:generateContent?key=" + apiKey; // Cambiar el modelo a 1.5 Flash

        //PROMPT PARA INDICARLE EL FORMATO DE LA RESPUESTA A LOS MENSAJES DEL USER
        String prompt = "Haz un titulo de un chat para el mensaje que te he enviado de maximo tres palabras, solo quiero que me devuelvas estrictamente esto, nada más.";

        String fullPrompt = message + "\n\n" + prompt;


        //Construímos el cuerpo de la petición
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", fullPrompt))
            ))
        ); //Creamos un mapa con la estructura que espera la API

        //llamamos a la api de gemini
        return webClient.post().uri(apiUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //Especificamos el tipo de contenido, en este caso JSON
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class) // Convertimos la respuesta en String (puede ajustarse según el JSON de Gemini)
        .block();
    }

   
}
