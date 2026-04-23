package com.example.secretpanda.ui.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import com.example.secretpanda.R;

import java.util.HashMap;

public class EfectosManager {

    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> sonidosMap = new HashMap<>();
    private static boolean cargado = false;
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void inicializar(Context context) {
        if (soundPool == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();

            // Cargamos todos los sonidos que vayas a usar en la app
            sonidosMap.put(R.raw.sonido_click, soundPool.load(context, R.raw.sonido_click, 1));
            sonidosMap.put(R.raw.sonido_aceptar, soundPool.load(context, R.raw.sonido_aceptar, 1));
            sonidosMap.put(R.raw.sonido_aplauso, soundPool.load(context, R.raw.sonido_aplauso, 1));
            sonidosMap.put(R.raw.sonido_disparo, soundPool.load(context, R.raw.sonido_disparo, 1));
            sonidosMap.put(R.raw.sonido_whoosh, soundPool.load(context, R.raw.sonido_whoosh, 1));
            sonidosMap.put(R.raw.sonido_fiasco, soundPool.load(context, R.raw.sonido_fiasco, 1));

            soundPool.setOnLoadCompleteListener((sp, id, status) -> cargado = true);
        }
    }

    /**
     * MÉTODO UNIVERSAL: Puedes llamarlo desde CUALQUIER hilo (incluso onResponse)
     */
    public static void reproducir(Context context, int resourceId) {
        // Usamos el mainHandler para que, se llame desde donde se llame,
        // el sonido se dispare en el hilo principal de la UI.
        mainHandler.post(() -> {
            if (!cargado || soundPool == null) return;

            Integer soundId = sonidosMap.get(resourceId);
            if (soundId == null) return; // El sonido no estaba precargado

            // 1. Bajamos volumen música (opcional, como prefieras)
            MusicaService.bajarVolumenParaEfecto();

            // 2. Leemos volumen de ajustes
            float vol = context.getSharedPreferences("Ajustes_Audio", Context.MODE_PRIVATE)
                    .getInt("volumen_efectos", 80) / 100f;

            // 3. Play
            soundPool.play(soundId, vol, vol, 1, 0, 1f);

            // 4. Subir música tras 300ms
            mainHandler.postDelayed(MusicaService::subirVolumenNormal, 300);
        });
    }
}