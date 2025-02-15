package com.api.api.DTO;

import com.api.api.model.Tip;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipDTO {
    //Hacemos un DTO para devolver solo el titulo de los tips en la sección de favoritos en el perfil del user
    private String title;

    public TipDTO(Tip tip){
        this.title = tip.getTitle();
    }
}
