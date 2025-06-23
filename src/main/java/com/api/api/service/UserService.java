package com.api.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.CloudinaryUploadDTO;
import com.api.api.DTO.FlagEntityDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.DTO.FormRequestDTO.ChangePasswordRequestDTO;
import com.api.api.DTO.UserDTO.UserResponseDTO;
import com.api.api.DTO.UserDTO.UserUpdateDTO;
import com.api.api.constants.ConfigFlags;
import com.api.api.constants.DailyFlags;
import com.api.api.constants.DerivedFlags;
import com.api.api.exceptions.UserAlreadyExistsException;
import com.api.api.model.ConfigurationUserFlags;
import com.api.api.model.DailyUserFlags;
import com.api.api.model.Onboarding;
import com.api.api.model.Sound;
import com.api.api.model.User;
import com.api.api.repository.ConfigurationUserFlagsRepository;
import com.api.api.repository.DailyUserFlagsRepository;
import com.api.api.repository.DrmRepository;
import com.api.api.repository.OnboardingRepository;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.SleepLogRepository;
import com.api.api.repository.TipRepository;
import com.api.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PatchUtils patchUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DailyUserFlagsRepository dailyUserFlagsRepository;

    @Autowired
    private ConfigurationUserFlagsRepository configurationUserFlagsRepository;

    @Autowired
    private DrmRepository drmRepository;

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private SleepLogRepository sleepLogRepository;

    @Autowired
    private SoundRepository soundRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //Función aux para centralizar la creación de banderas de configuración para el user
    private void createConfigurationFlag(User user, String flagKey, String flagValue) {
        ConfigurationUserFlags configurationUserFlags = new ConfigurationUserFlags();
        configurationUserFlags.setUser(user);
        configurationUserFlags.setFlagKey(flagKey);
        configurationUserFlags.setFlagValue(flagValue);
        this.configurationUserFlagsRepository.save(configurationUserFlags);
    }

    @Transactional
    public UserDTO.UserResponseDTO registerUser(User user) {

        //tenemos que comprobar que el user no exista ya en la BD
        User userRecuperado = this.userRepository.findByEmail(user.getEmail()).orElse(null);
        if (Objects.isNull(userRecuperado)){
            user.setPassword(this.passwordEncoder.encode(user.getPassword())); // Encripta la contraseña
            //Cuando un user crea una cuenta tenemos que poner valores por defecto tanto en el role (inmutable) como en la imagen de perfil (modificable en el futuro)
            user.setRole("USER");
            user.setProfilePicture("https://res.cloudinary.com/dtg2mkilx/image/upload/placeholder_jrnkvd.png");
            this.userRepository.save(user); //Guardamos el user en la BD

            //Tenemos que crear también el objeto Onboarding para el user y lo guardamos en la BD
            Onboarding onboarding = new Onboarding();
            onboarding.setUser(user);
            this.onboardingRepository.save(onboarding);

            //Creamos las banderas de configuración del user para gestionar su cuenta
            this.createConfigurationFlag(user, ConfigFlags.NOTIFICATIONS, Objects.toString(true));
            this.createConfigurationFlag(user, ConfigFlags.HAS_COMPLETED_ONBOARDING, Objects.toString(false));
            this.createConfigurationFlag(user, ConfigFlags.TIMER_DURATION, null);

            //Parseamos la info que se le devolverá al controller mediante su DTO correspondiente
            UserDTO.UserResponseDTO userResponseDTO = new UserDTO.UserResponseDTO(user);
            return userResponseDTO;
        }else{
            throw new UserAlreadyExistsException("El correo electrónico ya está en uso");
        }
    }

    //Función para recuperar la info de un user en la BD en base al id que se ha proporcionado
    @Transactional
    public User getUser(Long id){
        User userRecuperado = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return userRecuperado;
    }

    //Función para eliminar un user de la BD
    @Transactional
    public UserResponseDTO deleteUser(Long id){
        User user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Tenemos que eliminr todos los sonidos y la foto de perfil que tenga el user en cloudinary
        if (Objects.nonNull(user.getPublicIdCloudinary())) this.cloudinaryService.deleteFile(user.getPublicIdCloudinary(), false);
        //Recuperamos las entidades sonidos que están relacionadas con el user
        List<Sound> sounds = this.soundRepository.findByOwnerId(id);
        //Si no es vacía por sonido tenemos que llamar a la función de eliminar archivos de cloudinary
        if (!sounds.isEmpty()){
            sounds.forEach(sound -> {
                if (Objects.nonNull(sound.getPublicIdCloudinary())) this.cloudinaryService.deleteFile(sound.getPublicIdCloudinary(), true);
            });
        }
        //Ahora eliminamos toda la info relacionada con el user de la BD
        this.userRepository.delete(user);
        return new UserResponseDTO(user);
    }

    /*
     * Función para recuperar la info de un user en la BD en base al email que se ha proporcionado:
     * 
     * Es llamada por la función de registrar para verificar si existe ya un usuario con ese email y
     * también es llamada por el endpoint del login para recuperar la info del user y asi poder devolver el id en la respuesta
     * del endpoint junto a los tokens.
    */
    @Transactional
    public User getUserByEmail(String email){
        User userRecuperado = this.userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return userRecuperado;
    }

    //Función para actualizar la info permitida del user en la BD
    @Transactional
    public UserUpdateDTO updateUser(Long id, List<Map<String, Object>> updates) throws JsonPatchException{
        //Recuperamos el user de la BD y vemos si existe o no
        User user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //En caso de que el user exista comprobamos que en la lista de operaciones no haya ningun path de un atributo no modificable
        //Primero hacemos una lista de los paths que no se pueden modificar
        List<String> pathsNoModificables = List.of("/id", "/name","/age", "/role", "/email", "/profilePicture");

        for (Map<String,Object> update : updates) {
            String path = (String) update.get("path");
            //En caso de que el path de la operación que quiere hacer el user no este permitida devolvemos Bad Request
            if (pathsNoModificables.contains(path)){
                //Lanzamos excepcion de bad request
                throw new IllegalArgumentException("No se puede modificar el campo "+ path);
            }
            //Hacemos caso especial para el caso de que el user cambie la contraseña de la cuenta, la recibimos en texto plano y tenemos que encriptarla
            else if(path.equals("/password")){
                //El valor de la contraseña tiene que ser el DTO que se ha definido en el DTO de formRequest                
                ChangePasswordRequestDTO changePasswordRequestDTO = this.objectMapper.convertValue(update.get("value"), ChangePasswordRequestDTO.class);
                //tenemos que comprobar que la contraseña vieja que ha pasado el user tiene que ser la misma que la que está en la BD 
                String oldPassword = changePasswordRequestDTO.getOldPassword();
                if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) throw new IllegalArgumentException("La contraseña antigua no es correcta");
                //Una vez que se ha pasado esta comprobación miramos si la nueva contraseña es la misma que la que ya tiene el user, y si es así lanzamos una excepción
                String newPassword = changePasswordRequestDTO.getNewPassword();
                if (this.passwordEncoder.matches(newPassword, user.getPassword())) throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior");
                String encodedPassword = this.passwordEncoder.encode(newPassword);
                update.put("value", encodedPassword); //Actualizamos el valor de la contraseña que vamos a aplicar en el patch
            }
        }
        //Una vez comprobado los atributos que va a modificar el user, llamamos a patchUtils
        User userActualizado = this.patchUtils.patch(user, updates);
        //Guardamos el user actualizado en la BD
        this.userRepository.save(userActualizado);
        //Devolvemos el DTO indicando al user que se ha cambiado la contraseña correctamente
        return new UserUpdateDTO(true);
    }

    //Función para eliminar la foto de perfil de un user de la BD
    @Transactional
    public String deleteProfilePicture(Long idUser){
        //Recuperamos el user de la BD y vemos si existe o no
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Comprobamos que el user tenga una imagen de perfil diferente a la por defecto, en caso de que no la tenga lanzamos una excepción
        if (Objects.equals(user.getProfilePicture(), "https://res.cloudinary.com/dtg2mkilx/image/upload/placeholder_jrnkvd.png")) throw new IllegalArgumentException("El usuario no tiene una imagen de perfil personalizada");
        //Llamamos a la función de cloudinary para eliminar la imagen de la nube
        this.cloudinaryService.deleteFile(user.getPublicIdCloudinary(), false);
        //Una vez eliminada la imagen de la nube, actualizamos el campo de la imagen del user a la por defecto
        user.setProfilePicture("https://res.cloudinary.com/dtg2mkilx/image/upload/placeholder_jrnkvd.png");
        //Ponemos a null el public_id que hace referencia a la imagen de perfil que tenía el user para tener siempre un estado consistente en la BD
        user.setPublicIdCloudinary(null);
        //Guardamos el user actualizado en la BD
        this.userRepository.save(user);
        //Devolvemos la url del placeholder para que en el front se ponga de manera correcta
        return user.getProfilePicture();
    }

    //Función para actualizar la foto de perfil de un user de la BD
    @Transactional
    public UserUpdateDTO updateProfilePicture(Long idUser, MultipartFile file){
        //Recuperamos el user de la BD y vemos si existe o no
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Comprobamos que si el user tenía una foto antes, esta se borre tanto de la BD como de cloudinary
        if (Objects.nonNull(user.getPublicIdCloudinary())) this.cloudinaryService.deleteFile(user.getPublicIdCloudinary(), false);
        //Llamamos a la función de cloudinary para subir la nueva imagen y recuperar su public_id y url
        CloudinaryUploadDTO cloudinaryUploadDTO = this.cloudinaryService.uploadMultipartFile(file, false);
        user.setProfilePicture(cloudinaryUploadDTO.getUrl());
        user.setPublicIdCloudinary(cloudinaryUploadDTO.getPublicId());
        this.userRepository.save(user);
        return new UserUpdateDTO(user.getProfilePicture());
    }

    //Función para recuperar la lista de banderas que tiene el user
    @Transactional
    public Map<String, Map<String, Object>> getUserFlags(Long idUser){
        //Comprobamos que el user exista
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos la lista de banderas del user, para las diarias tenemos que buscar en el día de hoy

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        //Función para recuperar las banderas diarias del user que hace referencia a la entidad DailyUserFlags, excepto la hora en la que se ha ido a dormir que puede no estar en el día de hoy pero tiene una vida útil de 24 horas
        List<DailyUserFlags> dailyUserFlags = this.dailyUserFlagsRepository.findByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);

        //Si dentro de nuestro mapa no tenemos la bandera de sleepStart pq no está dentro de este día tenemos que comprobar si está en un plazo de 24 horas hacia atras
        if (dailyUserFlags.stream().noneMatch(flag -> flag.getFlagKey().equals(DailyFlags.SLEEP_START))){
            Optional<DailyUserFlags> sleepStartFlag = this.dailyUserFlagsRepository.findByUser_IdAndFlagKeyAndTimeStampBetween(idUser, DailyFlags.SLEEP_START, now.minusHours(24), now);
            if (sleepStartFlag.isPresent()) dailyUserFlags.add(sleepStartFlag.get());
        }
        
        //Recuperamos la lista de banderas de configuración del user
        List<ConfigurationUserFlags> configurationUserFlags = this.configurationUserFlagsRepository.findAllByUser_Id(idUser);
        /*
         * Recuperamos las banderas diarias que nos quedan que sacamos el valor de los correspondeintes repository
         * Para esto tenemos que consultar registros en el día de hoy en diferentes entidades: DRM, Tips y SleepLogs
        */

        //Pasamos las listas de banderas por el DTO correspondiente para devolver solo al user lo que le interesa
        //Recorremos las listas y creamos un DTO por cada elemento de ella, una vez creado lo añadimos a la lista que vamos a devolver
        List<FlagEntityDTO> dailyFlags = new ArrayList<>();
        List<FlagEntityDTO> configurationFlags = new ArrayList<>();

        Map<String, Map<String, Object>> userFlags = new LinkedHashMap<>();

        //Comprobamos que las listas que se han recuperado de los repository no sean nulas y en caso de no serlas las pasamos a un mapa correspondiente para guardar en el mapa que se va a devolver a la BD
        if (Objects.nonNull(dailyUserFlags) && !dailyUserFlags.isEmpty()){
            dailyFlags = dailyUserFlags.stream().map(dailyUserFlag -> new FlagEntityDTO(dailyUserFlag)).toList();
            Map<String, Object> dailyMap = new LinkedHashMap<>();
            for (FlagEntityDTO flag : dailyFlags){
                //En caso de que la bandera contenga un valor distinto de null en el expiryTime, añadimos dicho atributo también al mapa
                dailyMap.put(flag.getFlag(), flag.getValue());
                if (Objects.nonNull(flag.getExpiryTime())) dailyMap.put("expiry_"+flag.getFlag(), flag.getExpiryTime().toString());
            } 
            userFlags.put("dailyFlags", dailyMap);
        }
        if (Objects.nonNull(configurationUserFlags) && !configurationUserFlags.isEmpty()){
            configurationFlags = configurationUserFlags.stream().map(configurationUserFlag -> new FlagEntityDTO(configurationUserFlag)).toList();
            Map<String, Object> configMap = new LinkedHashMap<>();
            for (FlagEntityDTO flag : configurationFlags) configMap.put(flag.getFlag(), flag.getValue());
            userFlags.put("configurationFlags", configMap);
        } 

        boolean reportFlag = this.drmRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);
        boolean tipFlag = this.tipRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);
        boolean sleepFlag = this.sleepLogRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);

       //Guardamos las banderas diarias en el mapa que se va a devolver al user, además tenemos que mandarle la fecha de expiración que es el final del día en el que e user ha guradado la entidad en la BD
       Map<String, Object> dailyDerivedMap = new LinkedHashMap<>();
       dailyDerivedMap.put(DerivedFlags.DRM_REPORT_TODAY, reportFlag);
       dailyDerivedMap.put("expiry_drm_report", endOfDay);
       dailyDerivedMap.put(DerivedFlags.TIP_OF_THE_DAY, tipFlag);
       dailyDerivedMap.put("expiry_tip_of_the_day", endOfDay);
       dailyDerivedMap.put(DerivedFlags.SLEEP_LOG_TODAY, sleepFlag);
       dailyDerivedMap.put("expiry_sleep_log", endOfDay);

        userFlags.put("dailyDerivedFlags", dailyDerivedMap);

        return userFlags;
    }
}
