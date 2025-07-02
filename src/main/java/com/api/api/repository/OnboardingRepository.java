package com.api.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.api.model.Onboarding;

@Repository
public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
    /*
    * Definimos la función para obtener el objeto Onboarding de un user dado su id
    * De esta forma, se delega la búsqueda a la capa de repositorio, manteniendo la separación de responsabilidades.
    * Esto es preferible a manipular manualmente el objeto de User para obtener las respuestas.
    */

    Optional<Onboarding> findByUser_Id(Long idUser);
}
