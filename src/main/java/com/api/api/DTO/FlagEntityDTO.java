package com.api.api.DTO;

import java.time.LocalDateTime;

import com.api.api.model.ConfigurationUserFlags;
import com.api.api.model.DailyUserFlags;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
/*
 * Clase que representa un DTO para una bandera, que puede ser diaria o de configuración.
 * 
 * Se usa @JsonInclude(JsonInclude.Include.NON_NULL) para que no se incluyan en el JSON
 * los campos que sean null, esto va a pasar cuando devolvemos las banderas de configuración
 * ya que no tienen un campo expiryTime.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlagEntityDTO {

    private String flag;
    private String value;
    private LocalDateTime expiryTime;

    public FlagEntityDTO(DailyUserFlags dailyUserFlags) {
        this.flag = dailyUserFlags.getFlagKey();
        this.value = dailyUserFlags.getFlagValue();
        this.expiryTime = dailyUserFlags.getExpiryTime();
    }

    public FlagEntityDTO(ConfigurationUserFlags configurationUserFlags) {
        this.flag = configurationUserFlags.getFlagKey();
        this.value = configurationUserFlags.getFlagValue();
    }

    public FlagEntityDTO(String flagKey, String flagValue) {
        this.flag = flagKey;
        this.value = flagValue;
    }
}
