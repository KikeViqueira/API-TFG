package com.api.api.repository;

import java.time.LocalDateTime;

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
    
}
