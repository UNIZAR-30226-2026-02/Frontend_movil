package com.example.secretpanda.ui.home;

import com.example.secretpanda.R;

public class GestorImagenes {
    public static int obtenerImagenManual(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) return R.drawable.baseline_emoji_events_24;

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
        
        // Si es un nombre de recurso directo (ej. "avatar_1")
        return 0; 
    }

    public static int getMedallaPorVictorias(int victorias) {
        // Temporalmante deshabilitado hasta tener los recursos drawable de las medallas
        /*
        if (victorias >= 100) {
            return R.drawable.medal_gold;
        } else if (victorias >= 50) {
            return R.drawable.medal_silver;
        } else if (victorias >= 10) {
            return R.drawable.medal_bronze;
        }
        */
        return R.drawable.baseline_emoji_events_24; 
    }
}
