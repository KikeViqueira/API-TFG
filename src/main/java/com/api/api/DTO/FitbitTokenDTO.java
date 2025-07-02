package com.api.api.DTO;

import com.api.api.model.FitbitToken;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FitbitTokenDTO {

    //DTO para devolver la info del user necesaria, en este caso devolvemos todo menos el id del registro de la BD que se genera a insertar la instancia en ella
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private String userIdFitbit;
    private String tokenType;
    private String scope;

    public FitbitTokenDTO(FitbitToken fitbitToken) {
        this.accessToken = fitbitToken.getAccessToken();
        this.expiresIn = fitbitToken.getExpiresIn();
        this.refreshToken = fitbitToken.getRefreshToken();
        this.userIdFitbit = fitbitToken.getUserIdFitbit();
        this.tokenType = fitbitToken.getTokenType();
        this.scope = fitbitToken.getScope();
    }
}
