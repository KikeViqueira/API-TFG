package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.api.DTO.TipDTO;
import com.api.api.model.Tip;
import com.api.api.model.User;
import com.api.api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encripta la contraseña
        //Cuando un user crea una cuenta tenemos que poner valores por defecto tanto en el role (inmutable) como en la imagen de perfil (modificable en el futuro)
        user.setRole("USER");
        user.setProfilePicture("http://localhost:8080/images/placeholder.jpg");
        return userRepository.save(user);
    }

    //Funcion para obtener un user en base a su Id
    public User getUser(Long id){
        return userRepository.findById(id).orElse(null);
    }

    //Función para actualizar la info de un user en la BD
    public User updateUser(User user){
        return userRepository.save(user);
    }

    //Recuperamos los tips guardados como favoritos por un user
    public List<TipDTO> getFavoritesTips(Long id){
        List<TipDTO> tips = new ArrayList<>();
        //Comprobamos si el user existe
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            //Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
            for (Tip tip: user.getFavoriteTips()) tips.add(new TipDTO(tip));
        }
        return tips;
    }

    //Función para eliminar un tip de la lista de favoritos del user
    public TipDTO deleteFavoriteTip(long userId, long idTip){
        //Comprobamos si el user existe
        User user = userRepository.findById(userId).orElse(null);
        if (user != null){
            //Comprobamos si el user tiene tiene el tip en la lista de favoritos
            if (!user.getFavoriteTips().isEmpty()){
                for (Tip tip : user.getFavoriteTips()){
                    if (tip.getId() == idTip){
                        //Eliminamos el tip en caso de que el user lo tenga en favs
                        user.getFavoriteTips().remove(tip);
                        userRepository.save(user);
                        TipDTO tipDTO = new TipDTO(tip);
                        return tipDTO;
                    }
                }
            }
        }
        return null;
    }

    //Función para añadir un tip a la lista de favoritos del user
    public TipDTO addFavoriteTip(Long idUser, Tip tip){
        //Comprobamos que el user existe
        User user = userRepository.findById(idUser).orElse(null);
        if (user != null){
            //Tenemos que comprobar si el tip no esta ya en la lista de favoritos
            if (!user.getFavoriteTips().contains(tip)){
                user.getFavoriteTips().add(tip);
                userRepository.save(user);
                return new TipDTO(tip);
            }
        }
        return null;
    }
}
