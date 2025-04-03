package com.api.api.repository;

import java.time.LocalDateTime;
import java.util.List;

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

    //Función para recuperar todos los chats de un usuario
    List<Chat> findByUserId(Long userId);

    //Función para recuperar todos los chats de un usuario dentro de un rango de fechas
    List<Chat> findByUser_IdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
}
