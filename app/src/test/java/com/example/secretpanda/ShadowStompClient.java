package com.example.secretpanda;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.List;

import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

// Interceptamos directamente la clase StompClient
@Implements(StompClient.class)
public class ShadowStompClient {

    // Anulamos connect() sin argumentos
    @Implementation
    protected void connect() {
        System.out.println("TEST: StompClient.connect() bloqueado para evitar OutOfMemory.");
    }

    // Anulamos connect() que recibe cabeceras
    @Implementation
    protected void connect(List<StompHeader> _headers) {
        System.out.println("TEST: StompClient.connect(headers) bloqueado para evitar OutOfMemory.");
    }

    // Anulamos disconnect por si acaso
    @Implementation
    protected void disconnect() {
        System.out.println("TEST: StompClient.disconnect() bloqueado.");
    }
}