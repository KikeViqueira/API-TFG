package com.api.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.api.DTO.CloudinaryUploadDTO;
import com.api.api.DTO.FlagEntityDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.DTO.FormRequestDTO.ChangePasswordRequestDTO;
import com.api.api.DTO.UserDTO.UserUpdateDTO;
import com.api.api.constants.DailyFlags;
import com.api.api.model.ConfigurationUserFlags;
import com.api.api.model.DailyUserFlags;
import com.api.api.model.Onboarding;
import com.api.api.model.User;
import com.api.api.repository.ConfigurationUserFlagsRepository;
import com.api.api.repository.DailyUserFlagsRepository;
import com.api.api.repository.DrmRepository;
import com.api.api.repository.OnboardingRepository;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDTO.UserResponseDTO registerUser(User user) {

        //tenemos que comprobar que el user no exista ya en la BD
        User userRecuperado = this.userRepository.findByEmail(user.getEmail()).orElse(null);
        if (Objects.isNull(userRecuperado)){
            user.setPassword(this.passwordEncoder.encode(user.getPassword())); // Encripta la contraseña
            //Cuando un user crea una cuenta tenemos que poner valores por defecto tanto en el role (inmutable) como en la imagen de perfil (modificable en el futuro)
            user.setRole("USER");
            user.setProfilePicture("http://localhost:8080/images/placeholder.jpg");
            this.userRepository.save(user); //Guardamos el user en la BD

            //Tenemos que crear también el objeto Onboarding para el user y lo guardamos en la BD
            Onboarding onboarding = new Onboarding();
            onboarding.setUser(user);
            this.onboardingRepository.save(onboarding);

            //Parseamos la info que se le devolverá al controller mediante su DTO correspondiente
            UserDTO.UserResponseDTO userResponseDTO = new UserDTO.UserResponseDTO(user);
            return userResponseDTO;
        }else{
            throw new IllegalArgumentException("El usuario ya existe");
        }
    }

    //Función para recuperar la info de un user en la BD en base al id que se ha proporcionado
    @Transactional
    public User getUser(Long id){
        User userRecuperado = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return userRecuperado;
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
        return new UserUpdateDTO(userActualizado, true);
    }

    //Función para eliminar la foto de perfil de un user de la BD
    @Transactional
    public void deleteProfilePicture(Long idUser){
        //Recuperamos el user de la BD y vemos si existe o no
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Comprobamos que el user tenga una imagen de perfil diferente a la por defecto, en caso de que no la tenga lanzamos una excepción
        if (Objects.equals(user.getProfilePicture(), "http://localhost:8080/images/placeholder.jpg")) throw new IllegalArgumentException("El usuario no tiene una imagen de perfil personalizada");
        //Ponemos a null el public_id que hace referencia a la imagen de perfil que tenía el user para tener siempre un estado consistente en la BD
        user.setPublicIdCloudinary(null);
        //Llamamos a la función de cloudinary para eliminar la imagen de la nube
        this.cloudinaryService.deleteFile(user.getPublicIdCloudinary(), false);
        //Una vez eliminada la imagen de la nube, actualizamos el campo de la imagen del user a la por defecto
        user.setProfilePicture("http://localhost:8080/images/placeholder.jpg");
        this.userRepository.save(user);
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
        return new UserUpdateDTO(user);
    }

    //Función para recuperar la lista de banderas que tiene el user
    @Transactional
    public Map<String, Map<String, String>> getUserFlags(Long idUser){
        //Comprobamos que el user exista
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos la lista de banderas del user
        List<DailyUserFlags> dailyUserFlags = this.dailyUserFlagsRepository.findByUser_Id(idUser);
        //Recuperamos la lista de banderas de configuración del user
        List<ConfigurationUserFlags> configurationUserFlags = this.configurationUserFlagsRepository.findByUser_Id(idUser);
        /*
         * Recuperamos las banderas diarias que nos quedan que sacamos el valor de los correspondeintes repository
         * Para esto tenemos que consultar registros en el día de hoy en diferentes entidades: DRM, Tips y SleepLogs
        */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        //Pasamos las listas de banderas por el DTO correspondiente para devolver solo al user lo que le interesa
        //Recorremos las listas y creamos un DTO por cada elemento de ella, una vez creado lo añadimos a la lista que vamos a devolver
        List<FlagEntityDTO> dailyFlags = dailyUserFlags.stream().map(dailyUserFlag -> new FlagEntityDTO(dailyUserFlag)).toList();
        List<FlagEntityDTO> configurationFlags = configurationUserFlags.stream().map(configurationUserFlag -> new FlagEntityDTO(configurationUserFlag)).toList();

        boolean reportFlag = this.drmRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);
        boolean tipFlag = this.tipRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);
        boolean sleepFlag = this.sleepLogRepository.existsByUser_IdAndTimeStampBetween(idUser, startOfDay, endOfDay);

        //hacemos primero el mapa de las banderas que estén en lista
        //Recorremos cada una de las listas y creamos un mapa con el flag y valor de cada uno de los elementos de la lista que estamos recorriendo
        Map<String, String> configMap = configurationFlags.stream().collect(Collectors.toMap(FlagEntityDTO::getFlag, FlagEntityDTO::getValue));

        Map<String, String> dailyMap = dailyFlags.stream().collect(Collectors.toMap(FlagEntityDTO::getFlag, FlagEntityDTO::getValue));
        //Añadimos al mapa de banderas diarias las banderas que hemos recuperado de los distintos repository
        dailyMap.put(DailyFlags.DRM_REPORT_TODAY, String.valueOf(reportFlag));
        dailyMap.put(DailyFlags.TIP_OF_THE_DAY, String.valueOf(tipFlag));
        dailyMap.put(DailyFlags.SLEEP_LOG_TODAY, String.valueOf(sleepFlag));

        Map<String, Map<String, String>> userFlags = Map.of(
            "configurationFlags", configMap,
            "dailyFlags", dailyMap
        );

        return userFlags;
    }
}
