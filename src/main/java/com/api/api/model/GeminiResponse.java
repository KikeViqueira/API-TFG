package com.api.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 *
 * Deserialización: Podemos usar ObjectMapper de Jackson para convertir el JSON en esta clase
 *
 * Las clases dentro de clases (clases anidadas) se usan para modelar una jerarquía o estructura más compleja,
 * como en este caso, donde GeminiResponse tiene subelementos como Candidate y Content.
 *
 * El uso de static significa que las clases anidadas no dependen de una instancia de la clase externa,
 * lo que las hace útiles cuando la clase anidada no necesita acceso a los miembros de la clase externa.
 * Esto mejora la claridad y organización del código.
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) //Campos que vengan en el JSON que no estén en la clase se ignorarán
public class GeminiResponse {
    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;
    private String modelVersion;


    @Getter
    @Setter
    public static class Candidate {
        private Content content;
        private String finishReason;
        private double avgLogprobs;


        @Getter
        @Setter
        public static class Content {
            private List<Part> parts;
            private String role;

            @Getter
            @Setter
            public static class Part {
                private String text;

            }
        }
    }

    @Getter
    @Setter
    public static class UsageMetadata {
        private int promptTokenCount;
        private int candidatesTokenCount;
        private int totalTokenCount;
        private List<TokenDetail> promptTokensDetails;
        private List<TokenDetail> candidatesTokensDetails;


        @Getter
        @Setter
        public static class TokenDetail {
            private String modality;
            private int tokenCount;

        }
    }
}