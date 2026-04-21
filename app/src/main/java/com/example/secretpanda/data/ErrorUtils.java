package com.example.secretpanda.data;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.example.secretpanda.data.model.ErrorResponse;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class ErrorUtils {
    private static final String TAG = "ErrorUtils";
    private static final Gson gson = new Gson();

    /**
     * Extrae el mensaje de error de una respuesta de OkHttp y lo muestra en un Toast.
     */
    public static void showErrorMessage(Activity activity, Response response) {
        if (response.body() == null) {
            showToast(activity, "Error desconocido (sin cuerpo de respuesta)");
            return;
        }

        try {
            String json = response.body().string();
            ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
            
            String messageToShow = (errorResponse != null && errorResponse.getMessage() != null) 
                    ? errorResponse.getFullErrorMessage() 
                    : "Error inesperado (Código: " + response.code() + ")";

            showToast(activity, messageToShow);
            Log.e(TAG, "Error del Backend: " + json);
        } catch (IOException e) {
            Log.e(TAG, "Error leyendo el cuerpo de la respuesta", e);
            showToast(activity, "Error de red al procesar la respuesta del servidor");
        } catch (Exception e) {
            Log.e(TAG, "Error parseando el JSON de error", e);
            showToast(activity, "Error interno al procesar el error del servidor");
        }
    }

    /**
     * Muestra un error genérico de fallo de conexión.
     */
    public static void showConnectionError(Activity activity, Exception e) {
        Log.e(TAG, "Error de conexión", e);
        showToast(activity, "No se pudo conectar con el servidor. Revisa tu conexión.");
    }

    private static void showToast(Activity activity, String message) {
        if (activity != null) {
            activity.runOnUiThread(() -> 
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            );
        }
    }
}
