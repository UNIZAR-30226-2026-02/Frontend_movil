package com.example.secretpanda.data;

public class NetworkConfig {
    // Para pruebas locales (DESCOMENTAR para usar local)
    //public static final String BASE_URL = "http://10.0.2.2:8080/api";
    //public static final String WS_URL = "ws://10.0.2.2:8080/ws/websocket";

    // Configuración para el despliegue en Azure (DESCOMENTAR para usar Azure)
    public static final String BASE_URL = "https://codenamesweb.azurewebsites.net/api";
    public static final String WS_URL = "wss://codenamesweb.azurewebsites.net/ws/websocket";
}
