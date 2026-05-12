package com.example.secretpanda.data;

import com.example.secretpanda.data.NetworkConfig;
import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketManager {

    public interface WebSocketConnectionListener {
        void onWebSocketConnected();
        void onWebSocketDisconnected();
        void onWebSocketError(Throwable exception);
    }

    private static final String TAG = "WebSocketManager";
    private static final String WEBSOCKET_URL = NetworkConfig.WS_URL;

    private static WebSocketManager instance;
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable; // Para gestionar múltiples suscripciones
    private List<WebSocketConnectionListener> listeners = new CopyOnWriteArrayList<>(); // Lista concurrente de listeners

    private WebSocketManager() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);
        compositeDisposable = new CompositeDisposable();
        setupLifecycleListener();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    private void setupLifecycleListener() {
        compositeDisposable.add(stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.i(TAG, "¡Conectado a STOMP con éxito!");
                            for (WebSocketConnectionListener listener : listeners) {
                                listener.onWebSocketConnected();
                            }
                            break;
                        case ERROR:
                            Log.e(TAG, "Error en la conexión STOMP", lifecycleEvent.getException());
                            for (WebSocketConnectionListener listener : listeners) {
                                listener.onWebSocketError(lifecycleEvent.getException());
                            }
                            break;
                        case CLOSED:
                            Log.i(TAG, "Conexión STOMP cerrada");
                            for (WebSocketConnectionListener listener : listeners) {
                                listener.onWebSocketDisconnected();
                            }
                            break;
                    }
                }, throwable -> {
                    Log.e(TAG, "Error de RxJava en el lifecycle", throwable);
                    for (WebSocketConnectionListener listener : listeners) {
                        listener.onWebSocketError(throwable);
                    }
                }));
    }

    @SuppressLint("CheckResult")
    public void conectar(String jwtToken) {
        if (stompClient.isConnected()) {
            Log.d(TAG, "STOMP cliente ya conectado. No se reconecta.");
            for (WebSocketConnectionListener listener : listeners) {
                listener.onWebSocketConnected(); // Notificar a los listeners si ya está conectado
            }
            return;
        }

        List<StompHeader> headers = new ArrayList<>();
        if (jwtToken != null && !jwtToken.isEmpty()) {
            headers.add(new StompHeader("Authorization", "Bearer " + jwtToken));
        }

        stompClient.connect(headers);
    }

    public void reconnect(String jwtToken) {
        Log.d(TAG, "Intentando reconectar STOMP...");
        desconectar();
        // Damos un pequeño respiro antes de intentar conectar de nuevo
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Interrupción durante la espera de reconexión", e);
        }
        conectar(jwtToken);
    }

    public void desconectar() {
        if (stompClient != null && stompClient.isConnected()) {
            Log.d(TAG, "Desconectando STOMP cliente.");
            stompClient.disconnect();
        }
        // Limpiamos los disposables
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        // Volvemos a configurar el listener para futuras conexiones
        setupLifecycleListener();
    }

    public StompClient getStompClient() {
        return stompClient;
    }

    public void addConnectionListener(WebSocketConnectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeConnectionListener(WebSocketConnectionListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return stompClient != null && stompClient.isConnected();
    }
}