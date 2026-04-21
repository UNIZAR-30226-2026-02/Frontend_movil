package com.example.secretpanda.data;

import com.example.secretpanda.data.NetworkConfig;
import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketManager {

    private static final String TAG = "WebSocketManager";
    // Usamos la URL centralizada de NetworkConfig
    private static final String WEBSOCKET_URL = NetworkConfig.WS_URL;

    private static WebSocketManager instance;
    private StompClient stompClient;
    private Disposable lifecycleDisposable;

    private WebSocketManager() {
        // Inicializamos el cliente STOMP usando OkHttp bajo el capó
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    @SuppressLint("CheckResult")
    public void conectar(String jwtToken) {
        if (stompClient.isConnected()) return;

        // 1. Añadimos el JWT a los headers de STOMP para la autenticación
        List<StompHeader> headers = new ArrayList<>();
        if (jwtToken != null && !jwtToken.isEmpty()) {
            headers.add(new StompHeader("Authorization", "Bearer " + jwtToken));
        }

        // 2. Escuchamos el estado de la conexión (Conectado, Error, Cerrado)
        lifecycleDisposable = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.i(TAG, "¡Conectado a STOMP con éxito!");
                            break;
                        case ERROR:
                            Log.e(TAG, "Error en la conexión STOMP", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.i(TAG, "Conexión STOMP cerrada");
                            break;
                    }
                }, throwable -> Log.e(TAG, "Error de RxJava en el lifecycle", throwable));

        // 3. ¡Conectamos!
        stompClient.connect(headers);
    }

    public void desconectar() {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
        if (lifecycleDisposable != null && !lifecycleDisposable.isDisposed()) {
            lifecycleDisposable.dispose();
        }
    }

    /**
     * Devuelve el cliente STOMP para poder suscribirte a temas (topics) desde tus Activities
     */
    public StompClient getStompClient() {
        return stompClient;
    }
}