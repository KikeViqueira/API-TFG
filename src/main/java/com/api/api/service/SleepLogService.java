package com.api.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.SleepLogAnswerDTO;
import com.api.api.constants.DailyFlags;
import com.api.api.enums.DayOfWeek;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.model.SleepLog;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.User;
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

    @Autowired
    private DailyUserFlagsService dailyUserFlagsService;

    @Transactional
    //Función para crear un nuevo registro de sueño y añadir a este las respuestas del cuestionario matutino
    public SleepLogAnswerDTO createSleepLog(Long userId, HashMap<String, String> answers) {
        //Primero comprobamos que el user exista y lo tenemos que guardar para poder vincularlo con el sleepLog que vamos a crear
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));

        //tenemos que comprobar que no haya un registro de sueño del user en el día al que se corresponde la hora de levantamiento ya que hace referencia al día que el user quiere marcar que ha dormido
        LocalDateTime wakeUpTime = LocalDateTime.parse(answers.get("wakeUpTime"));

        //Calculamos el inicio y fin del día correspondiente a la hora en la que el user se ha levantado
        LocalDateTime startOfDay = wakeUpTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        boolean alreadyExists = this.sleepLogRepository.existsByUser_IdAndTimeStampBetween(userId, startOfDay, endOfDay);
        if (!alreadyExists){
            SleepLog sleepLog = new SleepLog();
            sleepLog.setUser(user);
            //Tenemos que asignarle como timeStamp al SleepLog la hora en la que el user se ha levantado para tener los cuestionarios relacionados con su día de una manera correcta
            sleepLog.setTimeStamp(LocalDateTime.parse(answers.get("wakeUpTime")));
            /*
            * Guardamos la entidad en la BD para generarle un id y asi poder pasarle el objeto a la función que se encarga de guardar las respuestas en SleepLogAnswerService
            * 
            * Haciendo esto solo se hacen registros en la bd en la tabla de SleepLog si el usuario hace el cuestionario matutino, si no, no se hace ningún registro y se optimiza
            * el almacenamiento en la BD, si un día el user no lo hace pues simplemente no se guarda nada y así no se desperdicia espacio en la BD teniendo registros vacíos
            *
            * Tenemos que guardar el registro en este punto del código ya que si intentamos hacer antes el save la bandera siempre nos devolverá true
            */
            
            this.sleepLogRepository.save(sleepLog);
            //En caso de que no exista el registro delegamos la lógica en la función del servicio SleepLogAnswerService, donde le pasamos el objeto SleepLog y las respuestas
            SleepLogAnswerDTO sleepLogAnswerDTO = sleepLogAnswerService.saveAnswers(sleepLog, answers);

            //Una vez tenemos el objeto guardado lo que tenemos que hacer es eliminar la bandera de irse a dormir del user de ese día
            this.dailyUserFlagsService.deleteFlag(userId, DailyFlags.SLEEP_START);

            return sleepLogAnswerDTO;
        }else throw new RelationshipAlreadyExistsException("El usuario ya ha hecho el registro de sueño hoy");
    }

    @Transactional
    //Función para recuperar las respuestas al cuestionario matutino de un user de ese mismo día
    public SleepLogAnswerDTO getSleepLog(Long userId){
        //Comprobamos que el user exista y comprobamos que el SleepLog exista
        this.userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Comprobamos que exista el registro correspondiente al user y que se haya hecho en el día actual
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        //Recuperamos el registro de sueño del user
        SleepLog sleepLog = this.sleepLogRepository.findByUser_IdAndTimeStampBetween(userId, startOfDay, endOfDay).orElseThrow(() -> new EntityNotFoundException("El usuario no ha hecho el registro de sueño hoy"));
        //Una vez tenemos el registro del user podemos devolver su correspondiente respuesta
        return new SleepLogAnswerDTO(sleepLog.getSleepLogAnswer());
    }

    @Transactional
    //Función para recuperar la duración del sueño de un user durante los últimos 7 días
    public Map<String, Float> getSleepLogsDuration(Long userId) {
        //Comprobamos que el user exista
        this.userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));

        // Obtenemos la fecha de hoy (según la zona del sistema, ya que LocalDate.now() lo hace por defecto)
        LocalDate today = LocalDate.now();
        // Definimos el rango de los últimos 7 días (incluyendo hoy)
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startOfPeriod = startDate.atStartOfDay();
        LocalDateTime endOfPeriod = today.plusDays(1).atStartOfDay().minusNanos(1);

        //Recuperamos todos los registros de sueño del user dentro de la semana
        List<SleepLog> sleepLogs = this.sleepLogRepository.findByUser_IdAndTimeStampBetweenOrderByTimeStampAsc(userId, startOfPeriod, endOfPeriod);

        // Creamos un mapa temporal con la fecha y la duración
        Map<LocalDate, Float> durations = sleepLogs.stream()
                .collect(Collectors.toMap(
                        sleepLog -> sleepLog.getTimeStamp().toLocalDate(),//Extrae la fecha (sin hora) del campo timeStamp de cada registro.
                        sleepLog -> sleepLog.getSleepLogAnswer().getDuration()/60000, //Extrae la duración del sueño de cada registro y la manda al front en minutos.
                        /*
                         *SI HAY DOS REGISTROS DEL MISMO DÍA SE CONSERVA EL PRIMERO, Y SE IGNORA EL SEGUNDO. AUNQUE ESTO NO DEBERÍA PASAR.*/
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
        //Una vez que tenemos el mapa hecho usamos nuestro enumerador para devolver al front correctamente los días de la semana
        return DayOfWeek.convertWeekDays(results);
    }

    /* 
     * función para recuperar toda la info de los registros que ha hecho el user en los últimos 7 días (Esta función no está asociadad a ningún endpoint)
     * se ha creado para que servicios como el de DRM o Tips puedan obtener la info de una manera completa para poder enviar el contexto a la IA
     * */
    public Map<Long, SleepLogAnswer> getSleepLogsForContext(Long userId) {
        //Comprobamos que el user exista
        this.userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));

        // Obtenemos la fecha de hoy (según la zona del sistema, ya que LocalDate.now() lo hace por defecto)
        LocalDate today = LocalDate.now();
        // Definimos el rango de los últimos 7 días (incluyendo hoy)
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startOfPeriod = startDate.atStartOfDay();
        LocalDateTime endOfPeriod = today.plusDays(1).atStartOfDay().minusNanos(1);

        //Recuperamos todos los registros de sueño del user dentro de la semana
        List<SleepLog> sleepLogs = this.sleepLogRepository.findByUser_IdAndTimeStampBetweenOrderByTimeStampAsc(userId, startOfPeriod, endOfPeriod);

        //Creamos un mapa donde guardamos la info de cada uno de los registros
        Map<Long, SleepLogAnswer> results = new LinkedHashMap<>();

        for (SleepLog sleepLog : sleepLogs) {
            results.put(sleepLog.getId(), sleepLog.getSleepLogAnswer());
        }

        return results;
    }
}
