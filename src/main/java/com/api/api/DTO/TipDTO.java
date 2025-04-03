package com.api.api.DTO;

import java.util.List;

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
        //Objeto que vamos a devolver al user cuando se haga un tip o cuando se recuperen los tips a enseñar en la app
        private Long id;
        private String title;
        private String icon;
        private String description;

        public TipResponseDTO(Tip tip){
            this.id = tip.getId();
            this.title = tip.getTitle();
            this.icon = tip.getIcon();
            this.description = tip.getDescription();
        }

        public TipResponseDTO(TipGeneratedWithAiDTO tipDTO){
            this.id = tipDTO.getId();
            this.title = tipDTO.getTitle();
            this.icon = tipDTO.getIcon();
            this.description = tipDTO.getDescription();
        }

        //Tenemos que implementar el método de ToString para que al hacer un System.out.println(tipDTO) se imprima el objeto como queremos
        @Override
        public String toString() {
            return "TipResponseDTO{" +
                    "' title='" + title + '\'' +
                    ", icon='" + icon + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class TipGeneratedWithAiDTO {
        /*
         * Hacemos un DTO para guardar en el los valores que vienen del json String de la respuesta de la IA
         * Sirve como paso intermedio y despues asignar a las entidades de Tip y TipDetail los valores correspondientes
         * */
        private Long id;
        private String title;
        private String description;
        private String icon;
        private String fullDescription;
        private List<String> benefits;
        private List<String> steps;
    }
    
}
