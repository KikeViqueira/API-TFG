package com.api.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    //Definimos la función que nos devolverá la lista de mensajes asociados a un chat que se ha guardado en la BD, ordenamos por el propio ID de manera ascendiente
    List<Message> findByChat_IdOrderByIdAsc(Long chatId);

}
