package com.api.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.api.DTO.UserDTO;
import com.api.api.DTO.UserDTO.UserUpdateDTO;
import com.api.api.model.Onboarding;
import com.api.api.model.User;
import com.api.api.repository.OnboardingRepository;
import com.api.api.repository.UserRepository;
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
    private PatchUtils patchUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDTO.UserResponseDTO registerUser(User user) {

        //tenemos que comprobar que el user no exista ya en la BD
        User userRecuperado = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (userRecuperado == null){
            user.setPassword(passwordEncoder.encode(user.getPassword())); // Encripta la contraseña
            //Cuando un user crea una cuenta tenemos que poner valores por defecto tanto en el role (inmutable) como en la imagen de perfil (modificable en el futuro)
            user.setRole("USER");
            user.setProfilePicture("http://localhost:8080/images/placeholder.jpg");
            userRepository.save(user); //Guardamos el user en la BD

            //Tenemos que crear también el objeto Onboarding para el user y lo guardamos en la BD
            Onboarding onboarding = new Onboarding();
            onboarding.setUser(user);
            onboardingRepository.save(onboarding);

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
        User userRecuperado = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
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
        User userRecuperado = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return userRecuperado;
    }

    //Función para actualizar la info permitida del user en la BD
    @Transactional
    public UserUpdateDTO updateUser(Long id, List<Map<String, Object>> updates) throws JsonPatchException{
        //Recuperamos el user de la BD y vemos si existe o no
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        //En caso de que el user exista comprobamos que en la lista de operaciones no haya ningun path de un atributo no modificable
        //Primero hacemos una lista de los paths que no se pueden modificar
        List<String> pathsNoModificables = List.of("/id", "/name","/age", "/role", "/email");

        for (Map<String,Object> update : updates) {
            String path = (String) update.get("path");
            //En caso de que el path de la operación que quiere hacer el user no este permitida devolvemos Bad Request
            if (pathsNoModificables.contains(path)){
                //Lanzamos excepcion de bad request
                throw new IllegalArgumentException("No se puede modificar el campo "+ path);
            }
            //Hacemos caso especial para el caso de que el user cambie la contraseña de la cuenta, la recibimos en texto plano y tenemos que encriptarla
            else if(path.equals("/password")){
                String rawPassword = (String) update.get("value");
                //Comparamos si la contraseña que se quiere cambiar es la misma que la que ya tiene el user
                if (passwordEncoder.matches(rawPassword, user.getPassword())) throw new IllegalArgumentException("La contraseña nueva no puede ser igual a la anterior");
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String encodedPassword = passwordEncoder.encode(rawPassword);
                update.put("value", encodedPassword); //Actualizamos el valor de la contraseña que vamos a aplicar en el patch
            }else if(path.equals("/profilePicture")){
                //En caso de que el user quiera cambiar la imagen de perfil, comprobamos que la imagen que se quiere poner es válida
                String profilePicture = (String) update.get("value");
                if (profilePicture.equals(user.getProfilePicture())) throw new IllegalArgumentException("La imagen de perfil que se quiere poner es la misma que la que ya tiene el usuario");
            }
        }
        //Una vez comprobado los atributos que va a modificar el user, llamamos a patchUtils
        User userActualizado = patchUtils.patch(user, updates);
        //Guardamos el user actualizado en la BD
        userRepository.save(userActualizado);
        return new UserDTO.UserUpdateDTO(userActualizado);
    }

    
}
