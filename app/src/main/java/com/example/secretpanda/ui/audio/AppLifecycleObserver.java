package com.example.secretpanda.ui.audio;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class AppLifecycleObserver implements DefaultLifecycleObserver {

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // La app vuelve al primer plano
        MusicaService.reanudarMusica();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // La app se va al segundo plano (botón Home, cambio de app, etc.)
        MusicaService.pausarMusica();
    }
}
