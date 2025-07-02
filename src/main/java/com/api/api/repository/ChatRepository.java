package com.api.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    //Definimos la función para comprobar si existe una relación entre un chat y un user
    boolean existsByUserIdAndId(Long userId, Long chatId);

    /*
     * Tenemos que definir una función para saber si al chat que se le está intentando escribir es de hoy
     * ya aue solo el chat de hoy es en el único en el que se puede escribir.
     */

    boolean existsByIdAndDateBetween(Long id, LocalDateTime startOfDay, LocalDateTime endOfDay);

    //Función para devolver los chats que pertenecen al usuario y están en la lista de Ids, esta función se utilizará para el endpoint de eliminación de chats
    List<Chat> findByUserIdAndIdIn(Long userId, List<Long> chatIds);

    //Función para recuperar todos los chats de un usuario en orden descendente por fecha que se recibe en el pageable
    Page<Chat> findByUserId(Long userId, Pageable pageable);

    //Función para recuperar todos los chats de un usuario dentro de un rango de fechas en orden descendente por fecha esta función se usa para el mapa de contibucion que hay en el front tenemos que crear la misma función para que pueda usar pageable y usarla en otros lados
    List<Chat> findByUser_IdAndDateBetweenOrderByDateDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    //Función para recuperar todos los chats de un usuario dentro de un rango de fechas con paginación
    Page<Chat> findByUser_IdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //Función para saber si el user ha hecho un chat en el día de hoy
    boolean existsByUserIdAndDateBetween(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    //Función para obtener el chat de hoy de un usuario (si existe)
    Optional<Chat> findByUserIdAndDateBetween(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
