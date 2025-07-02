package com.api.api.constants;

import java.util.Set;

public final class DailyFlags {

    //CONSTANTES QUE HACEN REFERENCIA A LAS BANDERAS DIARIAS
    public static final String CHAT_ID_TODAY = "chatId";
    public static final String HAS_CHAT_TODAY = "hasChatToday";
    public static final String SLEEP_START = "sleepStart";

    //Hacemos el constructor privado para que no se pueda instanciar
    private DailyFlags() {
    }

    /**
     * DEFINIMOS LOS MÉTODOS QUE TENDRÁ LA CLASE:
     * 
     * isModifiableDailyFlag(): Banderas que se pueden modificar
     * isDeletableDailyFlag(): Banderas que se pueden eliminar
     */
    public static boolean isModifiableDailyFlag(String flag) {
        return Set.of(SLEEP_START).contains(flag);
    }

    public static boolean isDeletableDailyFlag(String flag) {
        return Set.of(SLEEP_START).contains(flag);
    }
}
