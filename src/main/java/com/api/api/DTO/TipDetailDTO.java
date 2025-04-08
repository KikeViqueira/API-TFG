package com.api.api.DTO;

import java.util.List;

import com.api.api.model.TipDetail;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TipDetailDTO {

    private String fullDescription;
    private List<String> benefits;
    private List<String> steps;

    public TipDetailDTO(TipDetail tipDetail) {
        this.fullDescription = tipDetail.getFullDescription();
        this.benefits = tipDetail.getBenefits();
        this.steps = tipDetail.getSteps();
    }

}
