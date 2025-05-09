package com.api.api.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.FitbitTokenDTO;
import com.api.api.DTO.FitBitDTO.FoodDTO;
import com.api.api.DTO.FitBitDTO.LevelDetailDTO;
import com.api.api.DTO.FitBitDTO.LevelsDTO;
import com.api.api.DTO.FitBitDTO.SleepDTO;
import com.api.api.DTO.FitBitDTO.SleepWeeklyDTO;
import com.api.api.DTO.FitBitDTO.SummaryDTO;
import com.api.api.enums.DayOfWeek;
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
     public SleepDTO getSleepTodayInfo(){
        /*
         * La ventaja del try-with-resources es que, al declarar el recurso dentro del paréntesis del try,
         *  éste se cierra automáticamente al finalizar el bloque, sin necesidad de hacerlo manualmente en un finally
         *  Si se declara fuera, se pierde ese beneficio y se tendría que cerrar el recurso. Por ello, es recomendable
         *  declararlo dentro del try.
         */  

            try (InputStream is = getClass().getResourceAsStream("/data/sleep.json")) {
            JsonNode root = this.objectMapper.readTree(is); //Leemos el JSON
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
     public FoodDTO getFoodInfo(){
        try (InputStream is = getClass().getResourceAsStream("/data/foods.json")) {
            JsonNode root = this.objectMapper.readTree(is);
            JsonNode caloriesArray = root.path("foods-log-caloriesIn");

            Map<String, Integer> caloriesByDay = new HashMap<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            caloriesArray.forEach(node -> {
                String dateStr = node.path("dateTime").asText();
                int calories = node.path("value").asInt();
                LocalDate date = LocalDate.parse(dateStr, formatter);
                String spanishNameDay = DayOfWeek.getSpanishNameFromDate(date);
                caloriesByDay.put(spanishNameDay, calories);
            });

            //Creamos el DTO que le vamos a devolver al user
            FoodDTO foodDTO = new FoodDTO(caloriesByDay);
            return foodDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el JSON de foods: "+ e.getMessage());
        }
     }

     // Función que va a devolver el resgistro semanal de horas de sueño del user
     public SleepWeeklyDTO getSleepWeeklyInfo(){
        try (InputStream is = getClass().getResourceAsStream("/data/sleepWeekly.json")) {
            JsonNode root = this.objectMapper.readTree(is);
            JsonNode sleepArray = root.path("sleep");

            Map<String, Integer> sleepByDay = new HashMap<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            sleepArray.forEach(node -> {
                String dateStr = node.path("dateOfSleep").asText();
                int minutesAsleep = node.path("minutesAsleep").asInt();
                LocalDate date = LocalDate.parse(dateStr, formatter);
                String spanishNameDay = DayOfWeek.getSpanishNameFromDate(date);
                sleepByDay.put(spanishNameDay, minutesAsleep);
            });

            //Creamos el DTO que le vamos a devolver al user
            SleepWeeklyDTO sleepDTO = new SleepWeeklyDTO(sleepByDay);
            return sleepDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el JSON de sleepWeekly: "+ e.getMessage());
        }
     }

     //Función para guardar la info recuperada del login del usuario en fitbit
     public FitbitTokenDTO saveToken(JsonNode node, Long idUser){

        //Antes de nada tenemos que comprobar si el user existe en nuestra base de datos, esto es para guardar psoteriormente el token en la BD y relacionarlo con el
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("User no encontrado"));

        /*
         * Comprobamos si el user ya tiene un token guardado en la BD, esto lo hacemos ya que al ser un mock de la
         * funcionalidad que podría ser la real solo tenemos un mock de token por lo que en el caso de que el user ya tenga la instancia
         * guardada en la BD no queremos que se hagan llamadas de guardado, no sigue las políticas de diseño pero es un mock de la funcionalidad
         * asi que sería una mejora a hacer en el futuro.
        */
        Optional<FitbitToken> fitbitToken = this.fitBitRepository.findByUser_Id(idUser);
        //Si el user no tiene un token guardado, lo devolvemos al controller
        if(fitbitToken.isPresent()){
            return new FitbitTokenDTO(fitbitToken.get());
        }else{
            //Una vez que recibimos la info creamos una instancia de FitbitToken y la guardamos en la BD
            FitbitToken fitbitTokenSave = new FitbitToken();
            fitbitTokenSave.setAccessToken(node.path("access_token").asText());
            fitbitTokenSave.setExpiresIn(node.path("expires_in").asLong());
            fitbitTokenSave.setRefreshToken(node.path("refresh_token").asText());
            fitbitTokenSave.setUserIdFitbit(node.path("user_id").asText());
            fitbitTokenSave.setTokenType(node.path("token_type").asText());
            fitbitTokenSave.setScope(node.path("scope").asText());

            //Asignamos el user a el objeto ya que es el lado dueño de la relación (maneja la FK)
            fitbitTokenSave.setUser(user);

            //Guardamos el token en la BD
            this.fitBitRepository.save(fitbitTokenSave);
            //Devolvemos al controller la info en el formato del DTO
            return new FitbitTokenDTO(fitbitTokenSave);
        }
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
