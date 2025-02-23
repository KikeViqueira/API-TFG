package com.api.api.DTO;

import com.api.api.model.Tip;
import lombok.Getter;
import lombok.Setter;


public class TipDTO {


    @Getter
    @Setter
    public static class TipFavDTO {
        //Hacemos un DTO para devolver solo el id y el titulo de los tips en la sección de favoritos en el perfil del user
        private Long id;
        private String title;

        public TipFavDTO(Tip tip){
            this.id = tip.getId();
            this.title = tip.getTitle();
        }
    }

    @Getter
    @Setter
    public static class TipResponseDTO {
        //Hacemos un DTO para indicar como tiene que serla respuesta de la api cuando creamos un tip
        private Long id;
        private String title;
        private String description;
        private String icon;

        public TipResponseDTO(Tip tip){
            this.id = tip.getId();
            this.title = tip.getTitle();
            this.description = tip.getDescription();
            this.icon = tip.getIcon();
        }
    }


    
}
