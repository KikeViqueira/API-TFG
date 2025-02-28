package com.api.api.DTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomErrorResponseDTO {

    private LocalDateTime timestamp;
    private String message;
    private String details;

    public CustomErrorResponseDTO(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }
    
}
