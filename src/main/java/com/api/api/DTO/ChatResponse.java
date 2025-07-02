package com.api.api.DTO;

public interface ChatResponse {
    /*
     * Interfaz que hemos creado para que en ciertos métodos de la clase ChatService
     * como addMessageToChat en vez de devolver un Object ya que tenemos dos devoluciones de DTO diferentes,
     * pues creamos esta interfaz que la van a implementar los dos DTOs que devolvemos en el método.
     * Así la función devolverá cualquier objeto que implementa esta interfaz.
     */

}
