package com.api.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.repository.TipRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.api.api.DTO.TipDTO;
import com.api.api.exceptions.NoContentException;
import com.api.api.model.Tip;
import com.api.api.model.TipDetail;
import com.api.api.model.User;

@Service
public class TipService {

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private UserRepository userRepository;

    /*
     * Estos primeros métodos son para la gestión de los tips en la app en base a la acción del user
     */
    //Función para obtener todos los tips de la BD
    public List<TipDTO.TipResponseDTO> getTips(){
        //En caso de que no haya tips devolvemos null
        List<Tip> tips = tipRepository.findAll();
        if (tips.isEmpty()) throw new EntityNotFoundException("No hay tips en la BD");
        else{
            //Hacemos la conversión
            List<TipDTO.TipResponseDTO> tipsResponse = new ArrayList<>();
            for (Tip tip : tips) tipsResponse.add(new TipDTO.TipResponseDTO(tip));
            return tipsResponse;
        }
    }

    //Función para obtener un tip en concreto por su id en la BD
    public Tip geTipByID(Long id){
        return tipRepository.findById(id).orElse(null);
    }

    //Función para obtener un tip en concreto por su título en la BD //TODO: Esta función se usa para cuando se esta intentando crear una entidad llamando a un endpoint y no podemos usar ID ya que este se genera cuando la entidad se guarda en la BD
    public Tip geTipByTitle(String title){
        return tipRepository.findByTitle(title).orElse(null);
    }

    //Función para guardar un tip en la BD
    public TipDTO.TipResponseDTO createTip(Tip tip){
        //primero tenemos que comprobar que el tip que se está intentando crear no existe ya en la BD
        if (geTipByTitle(tip.getTitle()) != null){
            throw new IllegalArgumentException("El tip que se está intentando crear ya existe.");
        }else{
            Tip tipCreado = tipRepository.save(tip);
            return new TipDTO.TipResponseDTO(tipCreado);
        }
    }

    //Función para eliminar un tip de la BD
    public TipDTO.TipResponseDTO deleteTip(Long id){
        Tip tipRecuperado = geTipByID(id);
        if (tipRecuperado == null) throw new EntityNotFoundException("El tip que se está intentando eliminar no existe.");
        else  {
            tipRepository.deleteById(id);
            return new TipDTO.TipResponseDTO(tipRecuperado);
        }
    }

    //Función para recuperar la info detallada de un tip
    public TipDetail getDetailsTip(Long id){
        //Comprobamos si el tip que se ha seleccionado tiene detalles
        if (tipRepository.existsByIdAndTipDetailIsNotNull(id)) return tipRepository.findById(id).get().getTipDetail();
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
    public List<TipDTO.TipFavDTO> getFavoritesTips(Long idUser){
        List<TipDTO.TipFavDTO> tips = new ArrayList<>();
        //Comprobamos si el user existe
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!user.getFavoriteTips().isEmpty()){
            //Pasamos cada uno de los tips a su DTO correspondiente, ya que en la sección de favoritos solo queremos mostrar el título
            for (Tip tip: user.getFavoriteTips()) tips.add(new TipDTO.TipFavDTO(tip));
            return tips;
        } else throw new NoContentException("EL usuario no tiene tips favoritos");
    }

    @Transactional
    //Función para eliminar un tip de la lista de favoritos del user
    public TipDTO.TipFavDTO deleteFavoriteTip(long userId, long idTip){
        //Comprobamos si el user existe y el tip tambien
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        if (user.getFavoriteTips().contains(tip)){
            //Eliminamos el tip en caso de que el user lo tenga en favs
            user.getFavoriteTips().remove(tip);
            userRepository.save(user);
            TipDTO.TipFavDTO tipFavDTO = new TipDTO.TipFavDTO(tip);
            return tipFavDTO;
        }else throw new EntityNotFoundException("No se ha encontrado el tip con id: "+idTip+" en la lista de favoritos del user");
    }

    @Transactional
    //Función para añadir un tip a la lista de favoritos del user
    public TipDTO.TipFavDTO addFavoriteTip(Long idUser, Long idTip){
        //Comprobamos que el user existe y el tip existen en la BD
        User user = userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Tip tip = tipRepository.findById(idTip).orElseThrow(() -> new EntityNotFoundException("Tip no encontrado"));
        //Tenemos que comprobar si el tip no esta ya en la lista de favoritos
        if (!user.getFavoriteTips().contains(tip)){
            user.getFavoriteTips().add(tip);
            userRepository.save(user);
            //No hace falta guardar nada en la entidad tip ya que la encargada de la relación es la de User, asique Hibernate ya se ocupa solo de mantener la relación
            return new TipDTO.TipFavDTO(tip);
        }else throw new IllegalArgumentException("El tip ya está en la lista de favoritos del user");
    }

}
