package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.api.DTO.SoundDTO;
import com.api.api.DTO.TipDTO;
import com.api.api.model.Sound;
import com.api.api.model.Tip;
import com.api.api.model.User;
import com.api.api.repository.SoundRepository;
import com.api.api.repository.TipRepository;
import com.api.api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private SoundRepository soundRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encripta la contraseña
        //Cuando un user crea una cuenta tenemos que poner valores por defecto tanto en el role (inmutable) como en la imagen de perfil (modificable en el futuro)
        user.setRole("USER");
        user.setProfilePicture("http://localhost:8080/images/placeholder.jpg");
        return userRepository.save(user);
    }

    //Funcion para obtener un user en base a su email, ya que el id se crea una vez se guarda en la BD
    public User getUser(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

    //Función para actualizar la info de un user en la BD
    public User updateUser(User user){
        return userRepository.save(user);
    }

    //Recuperamos los tips guardados como favoritos por un user
    public List<TipDTO> getFavoritesTips(String email){
        List<TipDTO> tips = new ArrayList<>();
        //Comprobamos si el user existe
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && !user.getFavoriteTips().isEmpty()) {
            //Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
            for (Tip tip: user.getFavoriteTips()) tips.add(new TipDTO(tip));
        }
        return tips;
    }

    //Función para eliminar un tip de la lista de favoritos del user
    public TipDTO deleteFavoriteTip(long userId, long idTip){
        //Comprobamos si el user existe y el tip tambien
        User user = userRepository.findById(userId).orElse(null);
        Tip tip = tipRepository.findById(idTip).orElse(null);
        if (user != null && tip != null && user.getFavoriteTips().contains(tip)){ //Comprobamos si el user tiene tiene el tip en la lista de favoritos
            //Eliminamos el tip en caso de que el user lo tenga en favs
            user.getFavoriteTips().remove(tip);
            userRepository.save(user);
            TipDTO tipDTO = new TipDTO(tip);
            return tipDTO;
        }
        return null;
    }

    //Función para añadir un tip a la lista de favoritos del user
    public TipDTO addFavoriteTip(Long idUser, Long idTip){
        //Comprobamos que el user existe y el tip existen en la BD
        User user = userRepository.findById(idUser).orElse(null);
        Tip tip = tipRepository.findById(idTip).orElse(null);
        if (user != null && tip != null){
            //Tenemos que comprobar si el tip no esta ya en la lista de favoritos
            if (!user.getFavoriteTips().contains(tip)){
                user.getFavoriteTips().add(tip);
                userRepository.save(user);
                //No hace falta guardar nada en la entidad tip ya que la encargada de la relación es la de User, asique Hibernate ya se ocupa solo de mantener la relación
                return new TipDTO(tip);
            }
        }
        return null;
    }
}
