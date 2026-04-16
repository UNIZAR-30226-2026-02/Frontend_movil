package com.example.secretpanda.ui.home;

import com.example.secretpanda.R;

public class GestorImagenes {
    public static int obtenerImagenManual(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) return R.drawable.panda_mago;

        switch (nombreImagen.toLowerCase()) {
            case "1":
            case "panda_mago":
                return R.drawable.panda_mago;
            case "2":
            case "panda_explorador":
                return R.drawable.panda_explorador;
            case "3":
            case "panda_buceador":
                return R.drawable.panda_buceador;
            case "4":
            case "panda_futurista":
                return R.drawable.panda_futurista;
            case "5":
            case "panda_bambu":
                return R.drawable.panda_bambu;
        }
        
        return R.drawable.panda_mago; 
    }

    public static String getStringIdFromResource(int resId) {
        if (resId == R.drawable.panda_mago) return "1";
        if (resId == R.drawable.panda_explorador) return "2";
        if (resId == R.drawable.panda_buceador) return "3";
        if (resId == R.drawable.panda_futurista) return "4";
        if (resId == R.drawable.panda_bambu) return "5";
        return "1";
    }

    public static int getMedallaPorVictorias(int victorias) {
        return R.drawable.baseline_emoji_events_24; 
    }
}
