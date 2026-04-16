package com.example.secretpanda.ui;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import com.example.secretpanda.R;

public class MusicaService extends Service {
    private MediaPlayer mediaPlayer;
    private static MusicaService instance;

    public static void setVolumen(float volumen) {
        if (instance != null && instance.mediaPlayer != null) {
            instance.mediaPlayer.setVolume(volumen, volumen);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mediaPlayer = MediaPlayer.create(this, R.raw.loop_musica);
        mediaPlayer.setLooping(true);
        // Ajustamos el volumen (de 0.0 a 1.0)
        SharedPreferences prefs = getSharedPreferences("Ajustes_Audio", MODE_PRIVATE);
        float volGuardado = prefs.getInt("volumen_fondo", 50) / 100f;
        mediaPlayer.setVolume(volGuardado, volGuardado);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Al iniciar el servicio, comienza la música
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        // START_STICKY hace que si el sistema mata el servicio por falta de RAM, lo reinicie solo
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Al detener el servicio, liberamos los recursos
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    // Baja el volumen al 10% de forma instantánea
    public static void bajarVolumenParaEfecto() {
        if (instance != null && instance.mediaPlayer != null) {
            // Obtenemos el volumen actual de los ajustes para no subirlo de más luego
            float volBase = instance.getSharedPreferences("Ajustes_Audio", MODE_PRIVATE)
                    .getInt("volumen_fondo", 50) / 100f;
            instance.mediaPlayer.setVolume(volBase * 0.1f, volBase * 0.1f);
        }
    }

    // Lo devuelve a su estado original
    public static void subirVolumenNormal() {
        if (instance != null && instance.mediaPlayer != null) {
            float volBase = instance.getSharedPreferences("Ajustes_Audio", MODE_PRIVATE)
                    .getInt("volumen_fondo", 50) / 100f;
            instance.mediaPlayer.setVolume(volBase, volBase);
        }
    }
}
