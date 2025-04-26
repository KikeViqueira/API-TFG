package com.api.api.controller;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import com.api.api.DTO.CustomErrorResponseDTO;
import com.api.api.exceptions.AIResponseGenerationException;
import com.api.api.exceptions.NoContentException;
import com.api.api.exceptions.RelationshipAlreadyExistsException;
import com.api.api.exceptions.TodayChatAlreadyExists;
import com.github.fge.jsonpatch.JsonPatchException;

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

    //Tenemos que manejar las excepciones de tipo patch cuando estamos intentando actualizar campos no permitidos en la BD
    @ExceptionHandler(JsonPatchException.class)
    public ResponseEntity<?> handleJsonPatchException(JsonPatchException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Error al aplicar el patch: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } 


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Petición construída de manera errónea: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } 

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContentException(NoContentException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "No hay contenido: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(error);
    } 

    @ExceptionHandler(RelationshipAlreadyExistsException.class)
    public ResponseEntity<?> handleRelationshipAlreadyExistsException(RelationshipAlreadyExistsException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "La relación entre las dos entidades ya existe: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } 

    @ExceptionHandler(TodayChatAlreadyExists.class)
    public ResponseEntity<?> handleTodayChatAlreadyExists(TodayChatAlreadyExists ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Vuelve mañana: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } 

    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Acceso denegado: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    
    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(MethodNotAllowedException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Método no permitido: " + ex.getHttpMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(AIResponseGenerationException.class)
    public ResponseEntity<?> handleAIResponseGenerationException(AIResponseGenerationException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Error API AI: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.OK).body(error); //Devolvemos el siguiente código ya que el error se produce en la api de terceros, no en la nuestra
    }

    /*
    * Caso especial que devuelve spring boot cuando se viola el contexto de validacion
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Error en la validación de los datos recibidos: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }


    //Manejo de excepciones de validación de campos, en caso de que el String que se ha mandado a la validación no se pueda transformar a número debido a que el formato del String no es el adecuado
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<?> handleNumberFormatException(NumberFormatException ex, HttpServletRequest request){
        //Creamos el objeto de error
        CustomErrorResponseDTO error = new CustomErrorResponseDTO(LocalDateTime.now(), "Error al convertir el String recibido a número: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
