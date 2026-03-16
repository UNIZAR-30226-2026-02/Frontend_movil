package com.example.secretpanda.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "SecretPandaPrefs";
    private static final String KEY_JWT = "jwt_token";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Guardar el token (llámalo cuando el usuario inicie sesión)
    public void saveToken(String token) {
        prefs.edit().putString(KEY_JWT, token).apply();
    }

    // Recuperar el token
    public String getToken() {
        return prefs.getString(KEY_JWT, null);
    }

    // Borrar el token (para cerrar sesión)
    public void clearToken() {
        prefs.edit().remove(KEY_JWT).apply();
    }
}