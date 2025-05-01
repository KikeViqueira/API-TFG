package com.api.api.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudinaryUploadDTO {
    private String url;
    private String publicId;
    private String resourceName;

    public CloudinaryUploadDTO(String url, String publicId, String resourceName){
        this.url = url;
        this.publicId = publicId;
        this.resourceName = resourceName;
    }
}
