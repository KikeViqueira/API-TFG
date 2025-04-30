package com.api.api.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudinaryUploadDTO {
    private String url;
    private String publicId;

    public CloudinaryUploadDTO(String url, String publicId){
        this.url = url;
        this.publicId = publicId;
    }
}
