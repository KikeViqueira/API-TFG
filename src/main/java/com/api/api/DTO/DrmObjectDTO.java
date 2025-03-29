package com.api.api.DTO;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.api.api.model.Drm;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DrmObjectDTO {

    //Campos del objeto Drm que vamos a devolver al user
    private Long id;

    private LocalDateTime timeStamp;

    private String report;


    public DrmObjectDTO(Drm drm) {
        this.id = drm.getId();
        this.timeStamp = drm.getTimeStamp();
        this.report = drm.getReport();
    }

    @Override
    public String toString() {
        return "DrmAnswersDTO{" + this.id + this.timeStamp + this.report + '}';
    }
    
}
