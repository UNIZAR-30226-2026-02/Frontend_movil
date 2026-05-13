package com.example.secretpanda.data;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.secretpanda.data.model.ErrorResponse;
import com.example.secretpanda.ui.auth.LoginActivity;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class ErrorUtils {
    private static final String TAG = "ErrorUtils";
    private static final Gson gson = new Gson();

    private static final Map<String, String> MENSAJES_ERROR = new HashMap<>();

    static {
        // 9.1. Sesión y Seguridad
        MENSAJES_ERROR.put("SESSION_INVALIDATED", "Seguridad: Se ha iniciado sesión en otro dispositivo.");
        MENSAJES_ERROR.put("GOOGLE_TOKEN_EXPIRED", "Credenciales caducadas. Vuelve a identificarte.");
        MENSAJES_ERROR.put("INACTIVE_ACCOUNT", "Esta cuenta ha sido desactivada por la central.");

        // 9.2. Gestión de Partidas
        MENSAJES_ERROR.put("PLAYER_ALREADY_IN_GAME", "Ya tienes una misión activa en curso.");
        MENSAJES_ERROR.put("LOBBY_FULL", "Sala llena. Capacidad máxima alcanzada.");
        MENSAJES_ERROR.put("GAME_ALREADY_STARTED", "La misión ya ha comenzado sin ti.");
        MENSAJES_ERROR.put("INVALID_ROOM_CODE", "Código de sala incorrecto o misión inexistente.");
        MENSAJES_ERROR.put("MISSING_THEME_PACK", "El líder no posee el paquete de cartas necesario.");
        MENSAJES_ERROR.put("TEAM_UNBALANCED", "Equipos desequilibrados. Imposible iniciar la misión.");

        // 9.3. Gameplay
        MENSAJES_ERROR.put("NOT_YOUR_TURN", "Alto ahí. Aún no es tu turno.");
        MENSAJES_ERROR.put("INVALID_ROLE_ACTION", "Esta acción está restringida para tu rango/rol.");
        MENSAJES_ERROR.put("INVALID_PHASE_ACTION", "Acción denegada en la fase actual de la misión.");
        MENSAJES_ERROR.put("WORD_ALREADY_REVEALED", "Ese objetivo ya ha sido descubierto previamente.");

        // 9.4. Social y Perfil
        MENSAJES_ERROR.put("TAG_TAKEN", "Ese nombre en clave ya está registrado por otro agente.");
        MENSAJES_ERROR.put("PROFANITY_DETECTED", "Comunicación interceptada: Lenguaje inapropiado.");
        MENSAJES_ERROR.put("ALREADY_FRIENDS", "Este agente ya pertenece a tu red de contactos.");

        // 9.5. Tienda e Inventario
        MENSAJES_ERROR.put("INSUFFICIENT_FUNDS", "Fondos insuficientes. Necesitas más balas.");
        MENSAJES_ERROR.put("ITEM_NOT_OWNED", "No tienes autorización para equipar este elemento.");
        MENSAJES_ERROR.put("ALREADY_OWNED", "Este recurso ya forma parte de tu inventario.");

        // 9.6. Errores de Sistema
        MENSAJES_ERROR.put("INTERNAL_SERVER_ERROR", "Fallo crítico en los servidores de la central.");
    }

    /**
     * Extrae el mensaje de error de una respuesta de OkHttp y lo muestra.
     * Cierra la sesión si es un error crítico de seguridad.
     */
    public static void showErrorMessage(Activity activity, Response response) {
        if (response.body() == null) {
            showToast(activity, "Error desconocido (sin cuerpo de respuesta)");
            return;
        }

        try {
            String json = response.body().string();
            ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);

            String messageToShow = "Error inesperado (Código: " + response.code() + ")";
            boolean forzarLogout = false;

            if (errorResponse != null) {
                String errorCode = errorResponse.getErrorCode();

                // Mapear código de error específico
                if (errorCode != null && MENSAJES_ERROR.containsKey(errorCode)) {
                    messageToShow = MENSAJES_ERROR.get(errorCode);

                    // Lógica de expulsión por seguridad (RF-1 Sesiones únicas)
                    if (errorCode.equals("SESSION_INVALIDATED") ||
                            errorCode.equals("GOOGLE_TOKEN_EXPIRED") ||
                            errorCode.equals("INACTIVE_ACCOUNT")) {
                        forzarLogout = true;
                    }
                } else if (errorResponse.getMessage() != null) {
                    messageToShow = errorResponse.getMessage(); // Fallback al original
                }
            }

            showToast(activity, messageToShow);
            Log.e(TAG, "Error del Backend: " + json);

            // Si la cuenta está invalidada, lo sacamos de la app automáticamente 🔥
            if (forzarLogout && activity != null) {
                activity.runOnUiThread(() -> {
                    TokenManager tokenManager = new TokenManager(activity);
                    tokenManager.clearToken();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                });
            }

        } catch (IOException e) {
            Log.e(TAG, "Error leyendo el cuerpo de la respuesta", e);
            showToast(activity, "Error de red al procesar la respuesta del servidor");
        } catch (Exception e) {
            Log.e(TAG, "Error parseando el JSON de error", e);
            showToast(activity, "Error interno al procesar el error del servidor");
        }
    }

    public static void showConnectionError(Activity activity, Exception e) {
        Log.e(TAG, "Error de conexión", e);
        showToast(activity, "Infracción de seguridad o caída del servidor.");
    }

    private static void showToast(Activity activity, String message) {
        if (activity != null) {
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            );
        }
    }
}