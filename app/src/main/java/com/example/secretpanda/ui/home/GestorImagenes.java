package com.example.secretpanda.ui.home;

import android.content.Context;
import com.example.secretpanda.R;

public class GestorImagenes {
    public static int obtenerImagenManual(String nombreImagen) {
        if (nombreImagen == null) return R.drawable.baseline_emoji_events_24;

        switch (nombreImagen.toLowerCase()) {
            case "1":
                return R.drawable.panda_mago;
            case "2":
                return R.drawable.panda_explorador;
            case "3":
                return R.drawable.panda_buceador;
            case "4":
                return R.drawable.panda_futurista;
            case "5":
                return R.drawable.panda_bambu;
        }
        return 0;
    }
}
