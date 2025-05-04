package com.api.api.constants;

public final class DailyFlags {

    //CONSTANTES QUE HACEN REFERENCIA A LAS BANDERAS DE CONFIGURACIÓN
    public static final String TIMER_DURATION = "preferredTimerDuration";
    public static final String HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding";
    public static final String NOTIFICATIONS = "notifications";

    //CONSTANTES QUE HACEN REFERENCIA A LAS BANDERAS DIARIAS
    public static final String CHAT_ID_TODAY = "chatId";
    public static final String HAS_CHAT_TODAY = "hasChatToday";
    public static final String SLEEP_START = "sleepStart";
    //Banderas diarias que se extraen de la BD en base a ciertos registros de ciertas entidades
    public static final String DRM_REPORT_TODAY = "reportFlag";
    public static final String SLEEP_LOG_TODAY = "sleepLog";
    public static final String TIP_OF_THE_DAY = "tipFlag";

    //Hacemos el constructor privado para que no se pueda instanciar
    private DailyFlags() {
    }
}
