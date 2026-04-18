package com.example.secretpanda.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "SecretPandaPrefs";
    private static final String KEY_JWT = "jwt_token";
    private static final String KEY_ID_GOOGLE = "id_google";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_JWT, token).apply();
    }

    public void saveIdGoogle(String idGoogle) {
        prefs.edit().putString(KEY_ID_GOOGLE, idGoogle).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_JWT, null);
    }

    public String getIdGoogle() {
        return prefs.getString(KEY_ID_GOOGLE, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_JWT).remove(KEY_ID_GOOGLE).apply();
    }
}