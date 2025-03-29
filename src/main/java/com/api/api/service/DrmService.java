package com.api.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.DrmObjectDTO;
import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.DTO.SaveAnswersDrmAndGenerateReportDTO;
import com.api.api.DTO.FormRequestDTO.DRMRequestDTO;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.model.Drm;
import com.api.api.model.DrmAnswer;
import com.api.api.model.GeminiResponse;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.User;
import com.api.api.repository.DrmRepository;
import com.api.api.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class DrmService {

    @Autowired
    private DrmRepository drmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SleepLogService sleepLogService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private DrmAnswerService drmAnswerService;

    @Transactional
    //Función que se encargará de guardar la respuesta del usuario al cuestionario en la BD y llamar a la IA para generar el informe
    public SaveAnswersDrmAndGenerateReportDTO generateReportAndSaveAnswers(Long userId, DRMRequestDTO drmRequestDTO){
        //primero tenemos que comprobar que el user exista en la BD
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));

        //Comprobamos que el user ya no haya hecho un cuestionario hoy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        boolean alreadyExists = drmRepository.existsByUser_IdAndTimeStampBetween(userId, startOfDay, endOfDay);

        if (!alreadyExists){
            /*
             * Ahora lo que tenemos que ex recuperar toda la info del user relacionada con el: (Onboarding, SleepLog de la ultima semana)
             * 
             * usamos la función del service de Onboarding para recuperar las respuestas del Onboarding
             * 
             * usamos la función del service de SleepLog para recuperar las respuestas del cuestionario matutino de la ultima semana, que este
             * solo devuelve las horas que el user ha dormido cada día de la útlma semana pero es lo que nos interesa ya que la idea de la sección de DRM
             * es sacar concluciones de como es de buena la toma de decisiones del user en relación a las horas que duerme.
             * 
             * Si el user no ha hecho ningún cuestionario en la última semana, no se le permitirá hacer el cuestionario DRM y se le devolverá un error
             * ya que esto puede dar lugar a que el informe generado no sea correcto/preciso.
             * 
             */

             Map<String, Float> sleepLogsLastWeek = sleepLogService.getSleepLogsDuration(userId);

             /*
              * Tenemos que comprobar si existe algún valor que no sea cero en el hashmap, si no, no se le permitirá hacer el cuestionario
              * para esto lo que hacemos es pasar la colección de los valores del mapa a stream y comprobamos mediante el método anyMatch
              * si hay algún valor que no sea cero, si no, no se le permitirá hacer el cuestionario
              * hay que tener en cuenta que el método al encontrar el primer valor que no sea cero, devolverá true y se saldrá del bucle
            */

             if (sleepLogsLastWeek.values().stream().anyMatch(value -> value != 0)){

                //Conseguimos el contexto completo de los SleepLog del User para un mayor contexto y mejor precisión en la respuesta de la IA para el informe
                Map<Long, SleepLogAnswer> sleepLogsForContext = sleepLogService.getSleepLogsForContext(userId);
                //recuperamos las respuestas del user al onboarding
                OnboardingAnswerDTO onboardingAnswerDTO = onboardingService.getOnboardingAnswers(userId);
                //llamamos ahora a la función de GeminiService que se encarga de generar el informe
                String response = geminiService.generateReport(sleepLogsLastWeek, sleepLogsForContext, onboardingAnswerDTO, drmRequestDTO, user);
                /*
                *De todo los campos que nos devuelve la api de Gemini, solo nos interesa el campo text
                *Para eso pasamos el JSON que hemos recibido a un objeto de la clase GeminiResponse mediante el uso de ObjectMapper de Jackson
                */
                String report = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
                    //Guardamos en un String la parte de la respuesta de la IA que nos interesa
                    report = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
                } catch (JsonProcessingException e) {
                    System.out.println("Error al pasar la respuesta de la API de la IA al objeto correspondiente: "+ e.getMessage());
                }
                //Creamos la instancia de Drm
                Drm drm = new Drm();
                drm.setUser(user);
                drm.setReport(report);
                //Lo guardamos en la BD para que cuando guardemos las respuestas del cuestionario tengamos el id de la entidad
                drmRepository.save(drm);
                //Guardamos las respuestas del formulario en la bd delegando la lógica en el service de DrmAnswerService
                List<DrmAnswer> drmAnswers = drmAnswerService.saveAnswers(drm, drmRequestDTO.getData());
                //Creamos el DTO a devolver al controller
                return new SaveAnswersDrmAndGenerateReportDTO(drmAnswers, report);

             }else throw new NoContentException("El usuario no ha hecho ningún cuestionario matutino en la última semana");

        }else throw new RelationshipAlreadyExistsException("El usuario ya ha realizado el cuestionario DRM hoy");
    }

    //Función para recuperar el cuestionario DRM que se ha hecho en el día de hoy
    public DrmObjectDTO getTodayDrm(Long userId){
        //Primero comprobamos que el user exista
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //una vez sabemos que el user existe lo que tenemos que hacer es comprobar si ha hecho el cuestionario de Drm hoy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        Drm drm = drmRepository.findByUser_IdAndTimeStampBetween(userId, startOfDay, endOfDay).orElseThrow(() -> new EntityNotFoundException("El usuario no ha hecho el cuestionario DRM hoy"));
        //Una vez recuperado devolvemos el DTO correspondiente
        return new DrmObjectDTO(drm);
    }

    //Función para recuperar el historial de cuestionarios DRM que ha hecho el user
    public List<DrmObjectDTO> getHistoricalDrm(Long userId){
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Recuperamos todos los cuestionarios DRM que ha hecho el user
        List<Drm> drms = drmRepository.findAllByUser_Id(userId);
        //Comprobamos que la lista recuperado tenga algún elemento
        if (!drms.isEmpty()){
            //Creamos una lista de DTOs para devolver al controller
            List<DrmObjectDTO> drmsDTO = new ArrayList<>();
            for (Drm drm : drms) drmsDTO.add(new DrmObjectDTO(drm));
            return drmsDTO;
        } else throw new EntityNotFoundException("El usuario no ha hecho ningún cuestionario DRM");
    } 
    
}
