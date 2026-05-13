package com.example.secretpanda.ui.audio;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import com.example.secretpanda.R;

public class MusicaService extends Service {
    private MediaPlayer mediaPlayer;
    private static MusicaService instance;

    public static int volumenMusicaActual = 50;
    public static int volumenEfectosActual = 80;

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
        float volBase = volumenMusicaActual / 100f;
        mediaPlayer.setVolume(volBase, volBase);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public static void bajarVolumenParaEfecto() {
        if (instance != null && instance.mediaPlayer != null) {
            float volBase = volumenMusicaActual / 100f;
            instance.mediaPlayer.setVolume(volBase * 0.1f, volBase * 0.1f);
        }
    }

    public static void subirVolumenNormal() {
        if (instance != null && instance.mediaPlayer != null) {
            float volBase = volumenMusicaActual / 100f;
            instance.mediaPlayer.setVolume(volBase, volBase);
        }
    }

    public static void pausarMusica() {
        if (instance != null && instance.mediaPlayer != null && instance.mediaPlayer.isPlaying()) {
            instance.mediaPlayer.pause();
        }
    }

    public static void reanudarMusica() {
        if (instance != null && instance.mediaPlayer != null && !instance.mediaPlayer.isPlaying()) {
            instance.mediaPlayer.start();
        }
    }
}