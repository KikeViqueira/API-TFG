package com.api.api.constants;

public class DerivedFlags {

    //Banderas diarias que se extraen de la BD en base a ciertos registros de ciertas entidades
    public static final String DRM_REPORT_TODAY = "reportFlag";
    public static final String SLEEP_LOG_TODAY = "sleepLog";
    public static final String TIP_OF_THE_DAY = "tipFlag";

    //Hacemos el constructor privado para que no se pueda instanciar
    private DerivedFlags() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
