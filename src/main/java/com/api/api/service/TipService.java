package com.api.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.repository.DrmRepository;
import com.api.api.repository.TipDetailRepository;
import com.api.api.repository.TipRepository;
import com.api.api.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.api.api.DTO.DrmObjectDTO;
import com.api.api.DTO.OnboardingAnswerDTO;
import com.api.api.DTO.TipDTO.*;
import com.api.api.exceptions.JsonMappingTipException;
import com.api.api.exceptions.NoContentException;
import com.api.api.model.GeminiResponse;
import com.api.api.model.SleepLogAnswer;
import com.api.api.model.Tip;
import com.api.api.model.TipDetail;
import com.api.api.model.User;

@Service
public class TipService {

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DrmRepository drmRepository;

    @Autowired
    private TipDetailRepository tipDetailRepository;

    @Autowired
    private SleepLogService sleepLogService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private DrmService drmService;

    @Autowired
    private GeminiService geminiService;

    /*
     * Estos primeros métodos son para la gestión de los tips en la app en base a la acción del user
     */
    //Función para obtener todos los tips de la BD relacionados con un user
    public List<TipResponseDTO> getTips(Long idUser){
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Recuperamos los tips del user
        List<Tip> tips = this.tipRepository.findByUser_Id(idUser);
        if (tips.isEmpty()) throw new EntityNotFoundException("No hay tips en la BD que se correspondan con el user");
        else{
            //Hacemos la conversión
            List<TipResponseDTO> tipsResponse = new ArrayList<>();
            for (Tip tip : tips) tipsResponse.add(new TipResponseDTO(tip));
            return tipsResponse;
        }
    }

    //Función para guardar un tip en la BD
    public TipGeneratedDTO createTip(Long userId){
        User user = this.userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        /**
         * Para poder generar el tip el usuario ha tenido que hacer lo siguiente:
         * 1. Hacer el onboarding
         * 2. Hacer el cuestionario DRM de hoy, ya que es ahí donde se genera un tip personalizado para el user
         * 3. Tener un sleep log en lo que llevamos de semana mínimo
         */

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        boolean exists = drmRepository.existsByUser_IdAndTimeStampBetween(userId, startOfDay, endOfDay);

        if(exists){
            //Si existe lo tenemos que recuperar
            DrmObjectDTO drmObjectDTO = this.drmService.getTodayDrm(userId);

            //Recuperamos los sleep logs de la semana
            Map <String, Float> sleepLogsLastWeek = this.sleepLogService.getSleepLogsDuration(userId);
            //Comprobamos que haya el menos un registro válido de lo que hemos recuperado
            if (sleepLogsLastWeek.values().stream().anyMatch(value -> value != 0)){
                //Conseguimos tanto el onboarding como las respuestas completas a esos sleep logs válidos para que la IA tenga un mayor contexto
                Map<Long, SleepLogAnswer> sleepLogsForContext = this.sleepLogService.getSleepLogsForContext(userId);
                OnboardingAnswerDTO onboarding = this.onboardingService.getOnboardingAnswers(userId);

                /*
                 * En caso de que el user tenga más tips personalizados, los recuperamos y se los pasamos a la IA para que los tenga en cuenta a la hora de generar el nuevo tip
                 * 
                 * En este caso tenemos que llamar al repository y no llamar al método getTips ya que este lanza en caso de que no haya tips en la BD una excepción y no queremos eso
                 * , ya que puede ser que el user tenga haya hecho el resto de cuestionarios pero que nunca haya generado un tip
                */
                List<TipResponseDTO> existingTips = new ArrayList<>();
                List<Tip> existingTipsList = this.tipRepository.findByUser_Id(userId);
                if (!existingTipsList.isEmpty()){
                    for (Tip tip : existingTipsList) existingTips.add(new TipResponseDTO(tip));
                }

                //LLAMAMOS A LA FUNCIÖN DE LA API PARA QUE GENERE EL TIP PERSONALIZADO
                String response = this.geminiService.generateTip(sleepLogsLastWeek, sleepLogsForContext, onboarding, drmObjectDTO, user, existingTips);
                /*
                *De todo los campos que nos devuelve la api de Gemini, solo nos interesa el campo text
                *Para eso pasamos el JSON que hemos recibido a un objeto de la clase GeminiResponse mediante el uso de ObjectMapper de Jackson
                */
                String tipGenerated = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
                    //Guardamos en un String la parte de la respuesta de la IA que nos interesa (Se supone que va a tener formato JSON obligatorio)
                    tipGenerated = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
                    System.out.println("Response de la IA: "+ tipGenerated);

                } catch (JsonProcessingException e) {
                    System.out.println("Error al pasar la respuesta de la API de la IA al objeto correspondiente: "+ e.getMessage());
                }
                //Comprobamos que el tip generado no sea null
                if (Objects.nonNull(tipGenerated)){
                     //Una vez tenemos la respuesta lo que tenemos que hacer es parsearla a formato Json y guardar la info en la entidad correspondiente (De esta lógica se encarga el método saveTipFromJson)
                    TipGeneratedDTO newTipGenerated = saveTipFromJson(tipGenerated, user);
                    //En el caso de que se haya guardado correctamente devolvemos la respuesta, en caso contrario lanzamos una excepción
                    if (Objects.nonNull(newTipGenerated)) return newTipGenerated;
                    else throw new JsonMappingTipException("Error al parsear la respuesta de la IA en su entidad correspondiente");
                } else throw new NoContentException("La IA no ha devuelto un tip personalizado para el usuario");
            } else throw new NoContentException("El usuario no ha hecho ningún cuestionario matutino en la última semana");
        } else throw new NoContentException("El usuario no ha hecho el cuestionario DRM de hoy, por lo que no se puede generar un tip personalizado para él.");
    }

    /*
     * Método que recibe un JSON string, lo parsea y guarda la entidad Tip junto con su TipDetail.
     *
     * @param jsonString JSON que contiene los campos de Tip y TipDetail.
     * @return La entidad TipResponseDTO persistida o null si ocurrió un error.
     */
    private TipGeneratedDTO saveTipFromJson(String jsonString, User user) {
        // Limpiar los delimitadores de triple backticks (y etiqueta "json" opcional) si existen
        String cleanedJson = jsonString.replaceAll("(?s)^```(json)?\\s*|```\\s*$", "").trim();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Parseamos el JSON a TipDTO
            TipGeneratedWithAiDTO tipDTO = mapper.readValue(cleanedJson, TipGeneratedWithAiDTO.class);

            // Creamos la entidad Tip y asignamos los campos correspondientes
            Tip tip = new Tip();
            tip.setTitle(tipDTO.getTitle());
            tip.setDescription(tipDTO.getDescription());
            tip.setIcon(tipDTO.getIcon());
            //Relacionamos el tip que se ha creado con el user que esta logeado en la app
            tip.setUser(user);

            // Creamos la entidad TipDetail y asignamos sus campos
            TipDetail tipDetail = new TipDetail();
            tipDetail.setFullDescription(tipDTO.getFullDescription());
            tipDetail.setBenefits(tipDTO.getBenefits());
            tipDetail.setSteps(tipDTO.getSteps());

            // Establecemos la relación bidireccional. Si guardasemos el tip y el detalle sin hacer esto cada uno tendría su propio id
            tip.setTipDetail(tipDetail);
            tipDetail.setTip(tip);

            // Persistimos las entidades
            this.tipRepository.save(tip);
            this.tipDetailRepository.save(tipDetail);

            return new TipGeneratedDTO(tipDTO);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Función para eliminar un tip de la BD
    public TipResponseDTO deleteTip(Long id){
        Tip tipRecuperado = this.tipRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("El tip que se está intentando eliminar no existe"));
        this.tipRepository.deleteById(id);
        return new TipResponseDTO(tipRecuperado);
    }

    //Función para recuperar la info detallada de un tip
    public TipDetail getDetailsTip(Long id){
        //Comprobamos si el tip que se ha seleccionado tiene detalles
        if (this.tipRepository.existsByIdAndTipDetailIsNotNull(id)) return this.tipRepository.findById(id).get().getTipDetail();
        else throw new EntityNotFoundException("El tip que se está intentando recuperar no tiene detalles.");
    }

    /*
     * Debemos poner @Transactional en los métodos del servicio cuando necesitemos que todas las operaciones de base de datos
     *  que se realizan en ese método se ejecuten como una sola transacción. Esto significa que si ocurre algún error en medio,
     *  se deshacen todas las operaciones, garantizando la consistencia de los datos. También es útil en métodos que cargan datos perezosamente
     * (lazy loading) para que las asociaciones se resuelvan correctamente mientras la transacción esté activa.
     * 
     * Los métodos que se presentan a continuación tienen que ver con la gestión de los tips favoritos de un user
     */

    @Transactional
    //Recuperamos los tips guardados como favoritos por un user
    public List<TipFavDTO> getFavoritesTips(Long idUser){
        List<TipFavDTO> tips = new ArrayList<>();
        //Comprobamos si el user existe
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos la lista de favoritos del user mediante el método que hemos creado en el repository
        List<Tip> favoriteTips = this.tipRepository.findByUser_IdAndIsFavoriteTrue(idUser);
        if (!favoriteTips.isEmpty()){
            //Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
            favoriteTips.forEach(tip -> tips.add(new TipFavDTO(tip)));
            return tips;
        } else throw new NoContentException("EL usuario no tiene tips favoritos");
    }

    @Transactional
    //Función para eliminar un tip de la lista de favoritos del user
    public TipFavDTO deleteFavoriteTip(long userId, long idTip){
        //Comprobamos si el user existe y el tip tambien
        this.userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = this.tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        //Comprobamos que el tip corresponda al user y esté marcado como favorito
        if (this.tipRepository.existsByUser_IdAndId(userId, idTip) && this.tipRepository.existsByIdAndIsFavoriteTrue(idTip)){
            tip.setFavorite(false);
            this.tipRepository.save(tip);
            TipFavDTO tipFavDTO = new TipFavDTO(tip);
            return tipFavDTO;
        }else throw new EntityNotFoundException("No se ha encontrado el tip con id: "+idTip+" en la lista de favoritos del user");
    }

    @Transactional
    //Función para añadir un tip a la lista de favoritos del user
    public TipFavDTO addFavoriteTip(Long idUser, Long idTip){
        //Comprobamos que el user existe y el tip existen en la BD
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = this.tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        //Tenemos que comprobar si el tip no está ya en la lista de favoritos y si pertenece al usuario
        if (this.tipRepository.existsByUser_IdAndId(idUser, idTip) && !this.tipRepository.existsByIdAndIsFavoriteTrue(idTip)){
            tip.setFavorite(true);
            this.tipRepository.save(tip);
            return new TipFavDTO(tip);
        }else throw new IllegalArgumentException("El tip ya está en la lista de favoritos del user");
    }

    /*
    ASI ESTABAN LAS FUNCIONES DE FAVORITOS CUANDO TENIAMOS LA RELACIÓN MUCHOS A MUCHOS ENTRE LOS TIPS Y LOS USERS
    @Transactional
    Recuperamos los tips guardados como favoritos por un user
    public List<TipDTO.TipFavDTO> getFavoritesTips(Long idUser){
        List<TipDTO.TipFavDTO> tips = new ArrayList<>();
        Comprobamos si el user existe
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!user.getFavoriteTips().isEmpty()){
            Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
            for (Tip tip: user.getFavoriteTips()) tips.add(new TipDTO.TipFavDTO(tip));
            return tips;
        } else throw new NoContentException("EL usuario no tiene tips favoritos");
    }

    @Transactional
    Función para eliminar un tip de la lista de favoritos del user
    public TipDTO.TipFavDTO deleteFavoriteTip(long userId, long idTip){
        Comprobamos si el user existe y el tip tambien
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        if (user.getFavoriteTips().contains(tip)){
            Eliminamos el tip en caso de que el user lo tenga en favs
            user.getFavoriteTips().remove(tip);
            userRepository.save(user);
            TipDTO.TipFavDTO tipFavDTO = new TipDTO.TipFavDTO(tip);
            return tipFavDTO;
        }else throw new EntityNotFoundException("No se ha encontrado el tip con id: "+idTip+" en la lista de favoritos del user");
    }

    @Transactional
    Función para añadir un tip a la lista de favoritos del user
    public TipDTO.TipFavDTO addFavoriteTip(Long idUser, Long idTip){
        Comprobamos que el user existe y el tip existen en la BD
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        Tenemos que comprobar si el tip no esta ya en la lista de favoritos
        if (!user.getFavoriteTips().contains(tip)){
            user.getFavoriteTips().add(tip);
            userRepository.save(user);
            No hace falta guardar nada en la entidad tip ya que la encargada de la relación es la de User, asique Hibernate ya se ocupa solo de mantener la relación
            return new TipDTO.TipFavDTO(tip);
        }else throw new IllegalArgumentException("El tip ya está en la lista de favoritos del user");
    }

     */

}
