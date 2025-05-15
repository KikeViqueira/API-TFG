package com.api.api.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.FlagEntityDTO;
import com.api.api.constants.DailyFlags;
import com.api.api.model.DailyUserFlags;
import com.api.api.model.User;
import com.api.api.repository.DailyUserFlagsRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class DailyUserFlagsService {

    @Autowired
    private DailyUserFlagsRepository dailyUserFlagsRepository;

    @Autowired
    private UserRepository userRepository;

    //Función para cambiar el valor de una bandera de las que se pueden modificar
    @Transactional
    public FlagEntityDTO changeFlag(Long idUser, String flagKey, String flagValue) {
        User user = this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Verificamos que la bandera es modificable en el archivo de banderas de diarias
        if (!DailyFlags.isModifiableDailyFlag(flagKey)) throw new IllegalArgumentException("La bandera diaria no es modificable");
        /*
         * Como en este endpoint se modifican banderas que se crean dependiendo del día pues primero lo que tenemos que hacer es ver si existe
         * Esto no se hace en las de configuración ya que esas son fijas y se crean por defecto cuando el user hace la cuenta en la app
         * 
         * En este caso tenemos que:
         * 1. Verificar si existe la bandera en la base de datos
         * 2. Si no existe, la creamos
         * 3. Si existe, la modificamos
         */

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        DailyUserFlags dailyFlagRecovered = this.dailyUserFlagsRepository.findByUser_IdAndFlagKeyAndTimeStampBetween(idUser, flagKey, startOfDay, endOfDay).orElse(null);
        if (Objects.isNull(dailyFlagRecovered)){
            //En caso de que sea null tenemos que crearla y guardarla en la BD
            DailyUserFlags dailyFlag = new DailyUserFlags();
            dailyFlag.setFlagKey(flagKey);
            dailyFlag.setFlagValue(flagValue);
            dailyFlag.setUser(user);
            this.dailyUserFlagsRepository.save(dailyFlag);
            return new FlagEntityDTO(dailyFlag);
        }else{
            //En este caso existe
            dailyFlagRecovered.setFlagValue(flagValue);
            this.dailyUserFlagsRepository.save(dailyFlagRecovered);
            return new FlagEntityDTO(dailyFlagRecovered);
        }
    }

    //Función para eliminar una bandera diaria
    @Transactional
    public FlagEntityDTO deleteFlag(Long idUser, String flagkey){
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Verificamos que la bandera es eliminable en el archivo de banderas de diarias
        if (!DailyFlags.isDeletableDailyFlag(flagkey)) throw new IllegalArgumentException("La bandera diaria no es eliminable");
        //Tenemos que comprobar si el user tiene la bandera actualmente en la base de datos
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        DailyUserFlags dailyFlagToDelete = this.dailyUserFlagsRepository.findByUser_IdAndFlagKeyAndTimeStampBetween(idUser, flagkey, startOfDay, endOfDay).orElseThrow(() -> new EntityNotFoundException("La bandera diaria que se intenta eliminar no existe"));
        //Eliminamos la bandera de la BD
        this.dailyUserFlagsRepository.delete(dailyFlagToDelete);
        return new FlagEntityDTO(dailyFlagToDelete);
    }
}
