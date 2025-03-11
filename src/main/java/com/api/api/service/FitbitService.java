package com.api.api.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.FitBitDTO;
import com.api.api.DTO.FitbitTokenDTO;
import com.api.api.DTO.FitBitDTO.FoodDTO;
import com.api.api.DTO.FitBitDTO.LevelDetailDTO;
import com.api.api.DTO.FitBitDTO.LevelsDTO;
import com.api.api.DTO.FitBitDTO.SleepDTO;
import com.api.api.DTO.FitBitDTO.SummaryDTO;
import com.api.api.model.FitbitToken;
import com.api.api.model.User;
import com.api.api.repository.FitBitRepository;
import com.api.api.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class FitbitService {
   
    /*
     * Clase que se encarga de recoger la información de la API de Fitbit
     * (Por ahora puesta de manera estática) y devolversela al controlador para que lo muestre en el frontend
     * 
     * Solo se devuelve la información necesaria para el usuario
     * 
     */

     @Autowired
     private final ObjectMapper objectMapper = new ObjectMapper();

     @Autowired
     private FitBitRepository fitBitRepository;

     @Autowired
     private UserRepository userRepository;

     //Función que devuelve la información relacionada con el sueño del usuario
     public FitBitDTO.SleepDTO getSleepInfo(){
        /*
         * La ventaja del try-with-resources es que, al declarar el recurso dentro del paréntesis del try,
         *  éste se cierra automáticamente al finalizar el bloque, sin necesidad de hacerlo manualmente en un finally
         *  Si se declara fuera, se pierde ese beneficio y se tendría que cerrar el recurso. Por ello, es recomendable
         *  declararlo dentro del try.
         */  

            try (InputStream is = getClass().getResourceAsStream("/data/sleep.json")) {
            JsonNode root = objectMapper.readTree(is); //Leemos el JSON
            //Suponemos que el JSON tiene un array que se llama Sleep y tomamos su primer valor
            JsonNode sleepNode = root.path("sleep").get(0);
            SleepDTO sleepDTO = new SleepDTO();
            //Recogemos los valores correspondientes del JSON y los guardamos en los setter del DTO
            sleepDTO.setStartTime(sleepNode.path("startTime").asText());
            sleepDTO.setEndTime(sleepNode.path("endTime").asText());
            sleepDTO.setDuration(sleepNode.path("duration").asLong());
            sleepDTO.setEfficiency(sleepNode.path("efficiency").asInt());

            //Recogemos ahora los valores de los niveles de sueño (levels) y summary
            LevelsDTO levelsDTO = new LevelsDTO();
            SummaryDTO summaryDTO = new SummaryDTO();
            summaryDTO.setDeep(mapLevel(sleepNode.path("levels").path("summary").path("deep")));
            summaryDTO.setLight(mapLevel(sleepNode.path("levels").path("summary").path("light")));
            summaryDTO.setRem(mapLevel(sleepNode.path("levels").path("summary").path("rem")));
            summaryDTO.setWake(mapLevel(sleepNode.path("levels").path("summary").path("wake")));

            //Añadimos el objeto summary al objeto levels en su atributo de la clase SummaryDTO y añadimos este objeto al objeto SleepDTO en el mismo contexto
            levelsDTO.setSummary(summaryDTO);
            sleepDTO.setLevels(levelsDTO);

            return sleepDTO;
        }catch (Exception e){
            throw new RuntimeException("Error al leer el JSON de sleep: "+ e.getMessage());
        }
     }

     //Función que devuelve la información relacionada con la comida del usuario, en estecaso solo nos interesan las calorías
     public FitBitDTO.FoodDTO getFoodInfo(){
        try (InputStream is = getClass().getResourceAsStream("/data/foods.json")) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode foodNode = root.path("foods").get(0);
            //Creamos el DTO que le vamos a devolver al user
            FoodDTO foodDTO = new FoodDTO();
            //Recogemos los valores correspondientes del JSON y los guardamos en los setter del DTO
            foodDTO.setCalories(foodNode.path("calories").asInt());
            return foodDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el JSON de foods: "+ e.getMessage());
        }
     }

     //Función para guardar la info recuperada del login del usuario en fitbit
     public FitbitTokenDTO saveToken(JsonNode node, Long idUser){

        //Antes de nada tenemos que comprobar si el user existe en nuestra base de datos, esto es para guardar psoteriormente el token en la BD y relacionarlo con el
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("User no encontrado"));

        //Una vez que recibimos la info creamos una instancia de FitbitToken y la guardamos en la BD
        FitbitToken fitbitToken = new FitbitToken();
        fitbitToken.setAccessToken(node.path("access_token").asText());
        fitbitToken.setExpiresIn(node.path("expires_in").asLong());
        fitbitToken.setRefreshToken(node.path("refresh_token").asText());
        fitbitToken.setUserIdFitbit(node.path("user_id").asText());
        fitbitToken.setTokenType(node.path("token_type").asText());
        fitbitToken.setScope(node.path("scope").asText());

        //Asignamos el user a el objeto ya que es el lado dueño de la relación (maneja la FK)
        fitbitToken.setUser(user);

        //Guardamos el token en la BD
        fitBitRepository.save(fitbitToken);
        //Devolvemos al controller la info en el formato del DTO
        return new FitbitTokenDTO(fitbitToken);
     }


     //Funcion para que recibiendo un objeto del array de summary devuelva un objeto de tipo LevelDetailDTO
     private LevelDetailDTO mapLevel(JsonNode node) {
        LevelDetailDTO levelDetail = new LevelDetailDTO();
        levelDetail.setCount(node.path("count").asInt());
        levelDetail.setMinutes(node.path("minutes").asInt());
        levelDetail.setThirtyDayAvgMinutes(node.path("thirtyDayAvgMinutes").asInt());
        return levelDetail;
    }
    
}
