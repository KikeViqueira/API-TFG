package com.api.api.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.api.DTO.FlagEntityDTO;
import com.api.api.constants.ConfigFlags;
import com.api.api.model.ConfigurationUserFlags;
import com.api.api.repository.ConfigurationUserFlagsRepository;
import com.api.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ConfigurationUserFlagsService {

    @Autowired
    private ConfigurationUserFlagsRepository configurationUserFlagsRepository;

    @Autowired
    private UserRepository userRepository;

    //Función para cambiar el valor de una bandera de las que se pueden modificar
    @Transactional
    public FlagEntityDTO changeFlag(Long idUser, String flag, String flagValue) {
        //Verificamos que el usuario existe en la base de datos
        this.userRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("El usuario no existe"));
        //Verificamos que la bandera es modificable en el archivo de banderas de configuracion
        if (!ConfigFlags.isModifiableConfigFlag(flag)) throw new IllegalArgumentException("La bandera de configuración no es modificable");
        //Si es modificable, buscamos la bandera en la base de datos y cambiamos su valor por el nuevo
        ConfigurationUserFlags configurationUserFlags = this.configurationUserFlagsRepository.findByUser_IdAndFlagKey(idUser, flag);
        configurationUserFlags.setFlagValue(flagValue);
        this.configurationUserFlagsRepository.save(configurationUserFlags);
        //Devolvemos la bandera modificada
        return new FlagEntityDTO(configurationUserFlags);
    }
}
