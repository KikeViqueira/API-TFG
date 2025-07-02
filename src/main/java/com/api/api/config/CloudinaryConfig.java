package com.api.api.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.key}")
    private String apiKey;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        //Creamos el objeto Cloudinary con los datos de la API de Cloudinary
        return new Cloudinary(Map.of(
            "cloud_name", cloudName,
            "api_key",    apiKey,
            "api_secret", apiSecret
        ));
    }
}
