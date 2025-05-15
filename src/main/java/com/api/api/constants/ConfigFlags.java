package com.api.api.constants;

import java.util.Objects;
import java.util.Set;

public class ConfigFlags {

    //CONSTANTES QUE HACEN REFERENCIA A LAS BANDERAS DE CONFIGURACIÓN
    public static final String TIMER_DURATION = "preferredTimerDuration";
    public static final String HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding";
    public static final String NOTIFICATIONS = "notifications";

    //Hacemos el constructor privado para que no se pueda instanciar
    private ConfigFlags() {
    }

    //Método para comprobar si la bandera que quiere modificar el user lo es
    public static boolean isModifiableConfigFlag(String flag) {
        return Set.of(NOTIFICATIONS, TIMER_DURATION).contains(flag);
    }
}
