package com.api.api.DTO;

import java.util.List;

import com.api.api.model.Tip;
import com.api.api.model.TipDetail;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TipDetailDTO {

    private String fullDescription;
    private List<String> benefits;
    private List<String> steps;
    //Bandera para saber si el tip es favorito o no
    private boolean isFavorite;
    private String icon;
    private String color;
    private String title;

    //El parámetro booleano es para saber si el tip es favorito o no y viene de la propia entidad Tip que está relacionada con este detalle
    public TipDetailDTO(TipDetail tipDetail, Tip tip) {
        this.fullDescription = tipDetail.getFullDescription();
        this.benefits = tipDetail.getBenefits();
        this.steps = tipDetail.getSteps();
        this.isFavorite = tip.isFavorite();
        this.icon = tip.getIcon();
        this.color = tip.getColor();
        this.title = tip.getTitle();
    }

}
