package com.api.api.controller;

import java.net.http.HttpRequest;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.api.api.DTO.CustomErrorResponseDTO;
import com.api.api.exceptions.AIResponseGenerationException;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.RelationshipAlreadyExistsException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

/*
    * Para mantener los controllers limpios y evitar tener lógica de manejo de errores repetida,
    * lo ideal es centralizar el manejo de excepciones en una clase con @RestControllerAdvice. 
    * La idea es que la capa de servicio se encargue de lanzar excepciones con información que considere importante,
    * y el controlador global las intercepte para formar una respuesta adecuada.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Entidad no encontrada: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } 


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Conflicto: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } 

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContentException(NoContentException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "No hay contenido: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } 

    @ExceptionHandler(RelationshipAlreadyExistsException.class)
    public ResponseEntity<?> handleRelationshipAlreadyExistsException(RelationshipAlreadyExistsException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "La relación entre las dos entidades ya existe: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } 

    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Acceso denegado: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AIResponseGenerationException.class)
    public ResponseEntity<?> handleAIResponseGenerationExceptionn(AIResponseGenerationException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Error API AI: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.OK).body(error); //Devolvemos el siguiente código ya que el error se produce en la api de terceros, no en la nuestra
    }

    
}
