package com.api.api.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.SleepLogAnswerDTO;
import com.api.api.DTO.SleepLogRequestDTO;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.model.SleepLog;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.User;
import com.api.api.repository.SleepLogAnswerRepository;
import com.api.api.repository.SleepLogRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class SleepLogService {

    @Autowired
    private SleepLogRepository sleepLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SleepLogAnswerService sleepLogAnswerService;

    @Transactional
    //Función para crear un nuevo registro de sueño y añadir a este las respuestas del cuestionario matutino
    public SleepLogAnswerDTO createSleepLog(Long userId, HashMap<String, String> answers) {
        //Primero comprobamos que el user exista y lo tenemos que guardar para poder vincularlo con el sleepLog que vamos a crear
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Sabemos que si hemos lleagdo a este punto las respuestas son correctas, por lo que podemos crear el nuevo registro de sueño
        SleepLog sleepLog = new SleepLog();
        sleepLog.setUser(user);
       
        //llamamos a la función que se encarga de guardar las respuestas en la tabla de SleepLogAnswers pero antes tenemos que calcular el inicio y fin del día
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime start = today.atStartOfDay(zone);
        ZonedDateTime endOfDay = today.plusDays(1).atStartOfDay(zone).minusNanos(1);

        boolean alreadyExists = sleepLogRepository.existsByUser_IdAndTimeStampBetween(userId, start, endOfDay);
        if (!alreadyExists){
            /*
            * Guardamos la entidad en la BD para generarle un id y asi poder pasarle el objeto a la función que se encarga de guardar las respuestas en SleepLogAnswerService
            * 
            * Haciendo esto solo se hacen registros en la bd en la tabla de SleepLog si el usuario hace el cuestionario matutino, si no, no se hace ningún registro y se optimiza
            * el almacenamiento en la BD, si un día el user no lo hace pues simplemente no se guarda nada y así no se desperdicia espacio en la BD teniendo registros vacíos
            *
            * Tenemos que guardar el registro en este punto del código ya que si intentamos hacer antes el save la bandera siempre nos devolverá true
            */
            sleepLogRepository.save(sleepLog);
            //En caso de que no exista el registro delegamos la lógica en la función del servicio SleepLogAnswerService, donde le pasamos el objeto SleepLog y las respuestas
            SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogAnswerService.saveAnswers(sleepLog, answers);
            return sleepLogAnswerDTO;
        }else throw new RelationshipAlreadyExistsException("El usuario ya ha hecho el registro de sueño hoy");
    }

    @Transactional
    //Función para recuperar las respuestas al cuestionario matutino de un user de ese mismo día
    public SleepLogAnswerDTO getSleepLog(Long userId, Long sleepLogId){
        //Comprobamos que el user exista y comprobamos que el SleepLog exista
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        sleepLogRepository.findById(sleepLogId).orElseThrow(() -> new EntityNotFoundException("El registro de sueño no existe"));
        //Comprobamos que exista el registro correspondiente al user y que se haya hecho en el día actual
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime start = today.atStartOfDay(zone);
        ZonedDateTime endOfDay = today.plusDays(1).atStartOfDay(zone).minusNanos(1);
        //Recuperamos el registro de sueño del user
        SleepLog sleepLog = sleepLogRepository.findByUser_IdAndTimeStampBetween(userId, start, endOfDay).orElseThrow(() -> new EntityNotFoundException("El usuario no ha hecho el registro de sueño hoy"));
        //Una vez tenemos el registro del user podemos devolver su correspondiente respuesta
        return new SleepLogAnswerDTO(sleepLog.getSleepLogAnswer());
    }

    @Transactional
    //Función para recuperar la duración del sueño de un user durante los últimos 7 días
    public Map<String, Float> getSleepLogsDuration(Long userId) {
        //Comprobamos que el user exista
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        // Definimos el rango de los últimos 7 días (incluyendo hoy)
        LocalDate startDate = today.minusDays(6);
        ZonedDateTime startOfPeriod = startDate.atStartOfDay(zone);
        ZonedDateTime endOfPeriod = today.plusDays(1).atStartOfDay(zone).minusNanos(1);

        //Recuperamos todos los registros de sueño del user dentro de la semana
        List<SleepLog> sleepLogs = sleepLogRepository.findByUser_IdAndTimeStampBetweenOrderByTimeStampAsc(userId, startOfPeriod, endOfPeriod);

        // Creamos un mapa temporal con la fecha y la duración
        Map<LocalDate, Float> durations = sleepLogs.stream()
                .collect(Collectors.toMap(
                        sleepLog -> sleepLog.getTimeStamp().toLocalDate(),//Extrae la fecha (sin hora) del campo timeStamp de cada registro.
                        sleepLog -> sleepLog.getSleepLogAnswer().getDuration(), //Extrae la duración del sueño de cada registro.
                        //TODO: SI HAY DOS REGISTROS DEL MISMO DÍA SE CONSERVA EL PRIMERO, Y SE IGNORA EL SEGUNDO. AUNQUE ESTO NO DEBERÍA PASAR.
                        (existing, replacement) -> existing
                ));

        // Recorremos cada uno de los 7 días y asignamos la duración correspondiente o 0 si no hay registro.
        Map<String, Float> results = new LinkedHashMap<>(); //Implementación de Map que preserva el orden de inserción, de modo que cuando se itera sobre él, los elementos se devuelven en el mismo orden en que fueron añadidos.
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            // Usamos el nombre del día de la semana (por ejemplo, MONDAY, TUESDAY, etc.)
            String dayOfWeek = date.getDayOfWeek().toString();
            results.put(dayOfWeek, durations.getOrDefault(date, 0f)); //En caso de que no haya registro, se asigna 0.
        }
        return results;
    }
}
