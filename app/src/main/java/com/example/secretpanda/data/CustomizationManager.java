package com.example.secretpanda.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class CustomizationManager {
    private static final String PREF_NAME = "secret_panda_customization";
    private static final String KEY_BORDER_COLOR = "marco_carta_equipado";
    private static final String KEY_BOARD_COLOR = "fondo_tablero_equipado";
    
    private SharedPreferences prefs;

    public CustomizationManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCustomizations(String borderColor, String boardColor) {
        SharedPreferences.Editor editor = prefs.edit();
        if (borderColor != null) editor.putString(KEY_BORDER_COLOR, borderColor);
        if (boardColor != null) editor.putString(KEY_BOARD_COLOR, boardColor);
        editor.apply();
    }

    public int getBorderColor() {
        String hex = prefs.getString(KEY_BORDER_COLOR, "#000000"); // Negro por defecto o lo que prefieras
        return parseHex(hex);
    }

    public int getBoardColor() {
        // En la web el fondo por defecto parece ser el de la carpeta Manila
        String hex = prefs.getString(KEY_BOARD_COLOR, "#F3E5AB"); 
        return parseHex(hex);
    }

    private int parseHex(String hex) {
        try {
            if (hex == null || hex.isEmpty()) return Color.TRANSPARENT;
            if (!hex.startsWith("#")) hex = "#" + hex;
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }
}
