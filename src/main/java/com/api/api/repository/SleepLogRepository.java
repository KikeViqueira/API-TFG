package com.api.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.SleepLog;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLog, Long> {

    /*
     * Tenemos que definir una función que nos permita buscar un registro de sueño por el id del usuario y la fecha
     * ya que el user suelo puede hacer el registro como máximo una vez al día
     * 
     * La función que vamos a definir se va a llamar existsByUser_IdAndTimeStampBetween y comprueba si el user que ha recibido por parámetros tiene
     * algún registro de sueño entre las fechas/horas que también ha recibido por parámetros, si es true sabemos que el user ya ha hecho el registro
     */

     boolean existsByUser_IdAndTimeStampBetween(Long userId, LocalDateTime start, LocalDateTime end);

     /*
     * Función para obtener el objeto SleepLog de un user dado su id y la fecha
     * Esta función solo se va a utilizar para comprobar si el user ha hecho el registro de sueño en el día actual
     * y que solo pueda ver sus respuestas si ha hecho el registro
     * asi no dejamos que acceda a respuestas anteriores
     */

     Optional<SleepLog> findByUser_IdAndTimeStampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /*
    * Definimos la función para obtener todos los objetos SleepLog de un user dado su id y un rango de fechas
    * De esta forma, se delega la búsqueda a la capa de repositorio, manteniendo la separación de responsabilidades.
    * Esto es preferible a manipular manualmente el objeto de User para obtener las respuestas.
    */

    List<SleepLog> findByUser_IdAndTimeStampBetweenOrderByTimeStampAsc(Long userId, LocalDateTime start, LocalDateTime end);
    
}
