package com.api.api.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
            "cloud_name", "dtg2mkilx",
            "api_key",    "819361863149117",
            "api_secret", "mK8Kbxkixwf781SeNCEzJEMgOrU"
        ));
    }
}
