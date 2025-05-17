package com.api.api.DTO;

import com.api.api.model.ConfigurationUserFlags;
import com.api.api.model.DailyUserFlags;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FlagEntityDTO {

    private String flag;
    private String value;

    public FlagEntityDTO(DailyUserFlags dailyUserFlags) {
        this.flag = dailyUserFlags.getFlagKey();
        this.value = dailyUserFlags.getFlagValue();
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
