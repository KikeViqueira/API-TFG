package com.api.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.api.DTO.ChatResponseDTO;
import com.api.api.DTO.SoundDTO;
import com.api.api.DTO.TipDTO;
import com.api.api.DTO.UserDTO;
import com.api.api.DTO.UserDTO.UserResponseDTO;
import com.api.api.DTO.UserDTO.UserUpdateDTO;
import com.api.api.exceptions.NoContentException;
import com.api.api.model.Chat;
import com.api.api.model.Message;
import com.api.api.model.Onboarding;
import com.api.api.model.Sound;
import com.api.api.model.Tip;
import com.api.api.model.User;
import com.api.api.repository.ChatRepository;
import com.api.api.repository.OnboardingRepository;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.TipRepository;
import com.api.api.repository.UserRepository;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private PatchUtils patchUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDTO.UserResponseDTO registerUser(User user) {

        //tenemos que comprobar que el user no exista ya en la BD
        if (userRepository.findByEmail(user.getEmail()) == null){
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

    /*
     * Debemos poner @Transactional en los métodos del servicio cuando necesitemos que todas las operaciones de base de datos
     *  que se realizan en ese método se ejecuten como una sola transacción. Esto significa que si ocurre algún error en medio,
     *  se deshacen todas las operaciones, garantizando la consistencia de los datos. También es útil en métodos que cargan datos perezosamente
     * (lazy loading) para que las asociaciones se resuelvan correctamente mientras la transacción esté activa.
     */

    @Transactional
    //Recuperamos los tips guardados como favoritos por un user
    public List<TipDTO.TipFavDTO> getFavoritesTips(String email){
        List<TipDTO.TipFavDTO> tips = new ArrayList<>();
        //Comprobamos si el user existe
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (!user.getFavoriteTips().isEmpty()){
                //Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
                for (Tip tip: user.getFavoriteTips()) tips.add(new TipDTO.TipFavDTO(tip));
                return tips;
            } else throw new NoContentException("EL usuario no tiene tips favoritos");
        } else throw new EntityNotFoundException("No hay tips favoritos para el usuario con email: "+email);
    }

    @Transactional
    //Función para eliminar un tip de la lista de favoritos del user
    public TipDTO.TipFavDTO deleteFavoriteTip(long userId, long idTip){
        //Comprobamos si el user existe y el tip tambien
        User user = userRepository.findById(userId).orElse(null);
        Tip tip = tipRepository.findById(idTip).orElse(null);
        if (user != null){ //Comprobamos si el user tiene tiene el tip en la lista de favoritos
            if (tip != null && user.getFavoriteTips().contains(tip)){
                //Eliminamos el tip en caso de que el user lo tenga en favs
                user.getFavoriteTips().remove(tip);
                userRepository.save(user);
                TipDTO.TipFavDTO tipFavDTO = new TipDTO.TipFavDTO(tip);
                return tipFavDTO;
            }else throw new EntityNotFoundException("No se ha encontrado el tip con id: "+idTip+" en la lista de favoritos del user");
            
        }else throw new EntityNotFoundException("No se ha encontrado al usuario con id: "+ userId);
    }

    @Transactional //TODO: TENEMOS QUE MARCAR EL METODO COMO TRANSACTIONAL CUANDO ACCEDE A UN ATRIBUTO QUE REPRESENTA UNA RELACION
    //Función para añadir un tip a la lista de favoritos del user
    public TipDTO.TipFavDTO addFavoriteTip(Long idUser, Long idTip){
        //Comprobamos que el user existe y el tip existen en la BD
        User user = userRepository.findById(idUser).orElse(null);
        Tip tip = tipRepository.findById(idTip).orElse(null);
        if (user != null){
            //Tenemos que comprobar si el tip no esta ya en la lista de favoritos
            if (tip != null && !user.getFavoriteTips().contains(tip)){
                user.getFavoriteTips().add(tip);
                userRepository.save(user);
                //No hace falta guardar nada en la entidad tip ya que la encargada de la relación es la de User, asique Hibernate ya se ocupa solo de mantener la relación
                return new TipDTO.TipFavDTO(tip);
            }else throw new IllegalArgumentException("El tip ya está en la lista de favoritos del user");
        } else throw new EntityNotFoundException("No se ha encontrado al usuario con id: "+idUser);
    }



    //TODO: FUNCIONES QUE SE USAN POR LOS ENDPOINTS RELACIONADOS CON LOS CHATS DEL USER
    //Función para recuperar el historial de chats de un usuario
    @Transactional //Para que no de error al hacer la consulta
    public List<ChatResponseDTO> getChats(Long idUser){
        //Comprobamos si el user existe en la BD
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (user.getChats().isEmpty()) throw new NoContentException("El usuario no tiene chats");
        else{
            List<ChatResponseDTO> chatsRecuperados = new ArrayList<>();
            for (Chat chat : user.getChats()) chatsRecuperados.add(new ChatResponseDTO(chat));
            return chatsRecuperados;
        }
    }

    //Función para eliminar uno o varios chats de un user
    @Transactional
    public List<ChatResponseDTO> deleteChats(Long idUser, List<Long> idChats){
        //Comprobamos si el user existe
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Recuperamos los chats que si que existen en la BD y comparamos longitudes para ver si estan todos o no
        List<Chat> chats = chatRepository.findAllById(idChats);
        if (chats.size() == idChats.size()){
            /*
            * LA LONGITUD DE AMBAS LISTAS SON IGUALES POR LO QUE PODEMOS ELIMINAR TODOS LOS CHATS*/
            user.getChats().removeAll(chats);
            userRepository.save(user);
            chatRepository.deleteAll(chats);
            List<ChatResponseDTO> chatsRecuperados = new ArrayList<>();
            for (Chat chat : chats) chatsRecuperados.add(new ChatResponseDTO(chat));
            return chatsRecuperados;
        } else throw new EntityNotFoundException("Uno o más chats no se han encontrado.");
    }

    //Función para recuperar la conversación de un chat
    @Transactional
    public List<Message> getChat(Long idUser, Long idChat){
        //Comprobamos si el user existe
        userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        //Comprobamos si el chat existe
        Chat chat = chatRepository.findById(idChat).orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));
        //Comprobamos si existe una relación entre el user y el chat
        if (chatRepository.existsByUserIdAndId(idUser, idChat)){
            //Devolvemos la lista de mensajes del chat
            return chat.getMessages();
        } else throw new IllegalArgumentException("El usuario no tiene acceso a este chat");
    }


}
