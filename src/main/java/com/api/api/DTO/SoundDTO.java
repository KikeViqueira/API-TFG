package com.api.api.DTO;

import com.api.api.model.Sound;
import lombok.Getter;
import lombok.Setter;
public class SoundDTO {

    @Getter @Setter
    public static class ResponseSoundDTO {
        /*
         * DTO para devolver los campos necesarios de un sonido en el frontEnd
         * se usará tanto cuando se recuperen los sonidos estáticos como cuando se recupere los sonidos de un user
         * Además de cuando el user cree un sonido.
         */
        private Long id;
        private String name;
        //tenemos que llamarle source ya que es como usamos la propiedad en el frontend
        private String source;
        private Boolean isDefault;
        private Boolean isLooping;

        public ResponseSoundDTO(Sound sound){
            this.id = sound.getId();
            this.name = sound.getName();
            this.source = sound.getFileUrl();
            this.isDefault = sound.isDefault();
            //Por defecto al cuando recuperemos o creeemos un sonido, no estará en bucle
            this.isLooping = false;
        } 
    }

    @Getter @Setter
    public static class DeleteSoundDTO {
        //DTO que devuelve la info de un sonido que se ha eliminado
        private Long id;
        private String name;
        private String source;

        public DeleteSoundDTO(Sound sound){
            this.id = sound.getId();
            this.name = sound.getName();
            this.source = sound.getFileUrl();
        }
    }
}
