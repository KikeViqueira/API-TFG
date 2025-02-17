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
import com.api.api.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

    //Recuperamos los sonidos que el user ha subido a la app
    public List<SoundDTO> getUserSounds(Long idUser){
        //tenemos que devolver la lista de sonidos de nuestra instancia User al controller
        List<SoundDTO> userSounds = new ArrayList<>();
        //tenemos que mirar si el user existe o no
        User user = userRepository.findById(idUser).orElse(null);
        if (user != null && !user.getSoundsUser().isEmpty()){
            for (Sound sound : user.getSoundsUser()) userSounds.add(new SoundDTO(sound));
        }
        return userSounds;
    }

    //Función para que el user pueda crear un sonido en la app
    public SoundDTO createSound(Long id, Sound sound){
        //Comprobamos si el user existe
        User user = userRepository.findById(id).orElse(null);
        //En caso de que el user exista y el sonido ya no este creado por el, lo metemos en la BD
        if(user != null && !user.getSoundsUser().contains(sound)) { //TODO: tenemos que implementar de manera correcta el método equals de la entidad sound
            user.getSoundsUser().add(sound);
            soundRepository.save(sound); //Guardamos el sonido en su tabla correspondiente
            SoundDTO sonidoCreado = new SoundDTO(sound);
            userRepository.save(user);//TODO: HACE FALTA ACTUALIZAR LA INFO DEL USER EN LA BD?????
            return sonidoCreado;
        }
        return null;
    }

    //Función para eliminar un sonido de lo sque ha subido el user a la app
    public SoundDTO deleteSoundUser(Long idUser, Long idSound){
        //Comprobamos que el user exista
        User user = userRepository.findById(idUser).orElse(null);
        if (user != null){
            //Comprobamos que el sonido exista en la bd
            Sound sound = soundRepository.findById(idSound).orElse(null);
            if (sound != null && user.getSoundsUser().contains(sound)){//Si el sonido existe en la BD y esta en la lista de sonidos del user que ha llamado al endpoint entonces podemos eliminarlo de la BD
                user.getSoundsUser().remove(sound);
                soundRepository.delete(sound);
                userRepository.save(user);
                return new SoundDTO(sound);
            }
        }
        return null;
    }
}
