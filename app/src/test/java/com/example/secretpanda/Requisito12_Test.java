package com.example.secretpanda;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.GridLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import android.content.Context;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class Requisito12_Test {

    @Before
    public void setUp() {
        // Configuramos el entorno inicial básico
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("SecretPandaPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("auth_token", "token_valido_test")
                .putString("id_google", "12345")
                .commit();
    }

    @Test
    public void testReglasBasicas_TableroDe25Cartas() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 1);
        intent.putExtra("MI_EQUIPO", "rojo");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {

            // 1. Damos un momento para que la actividad arranque su XML
            ShadowLooper.idleMainLooper();

            // 2. INYECCIÓN DIRECTA DEL ESTADO (Ignoramos OkHttp y WebSockets)
            // 2. INYECCIÓN DIRECTA DEL ESTADO
            scenario.onActivity(activity -> {
                try {
                    JSONObject estadoJuego = new JSONObject(generarJson25Cartas());
                    java.lang.reflect.Method method = activity.getClass().getDeclaredMethod("aplicarEstadoTotal", JSONObject.class);
                    method.setAccessible(true);
                    method.invoke(activity, estadoJuego);

                    // Forzamos a que la vista se actualice y mida inmediatamente en Robolectric
                    activity.findViewById(android.R.id.content).requestLayout();

                } catch (Exception e) {
                    throw new RuntimeException("ERROR AL INYECTAR ESTADO", e);
                }
            });

            // 3. Drenamos absolutamente todos los procesos de la UI pendientes
            ShadowLooper.idleMainLooper();
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // 4. Comprobación manual alternativa (esquiva problemas de timing de Espresso)
            scenario.onActivity(activity -> {
                GridLayout grid = activity.findViewById(R.id.grid_tablero);
                int hijos = grid != null ? grid.getChildCount() : -1;

                if (hijos != 25) {
                    throw new AssertionError("El tablero no tiene 25 cartas. Tiene: " + hijos + ". " +
                            "Revisa los logs (Logcat/Consola) para ver si 'aplicarEstadoTotal' lanzó alguna excepción silenciosa.");
                }
            });
        }
    }

    private String generarJson25Cartas() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"estado\": \"jugando\", ")
                .append("\"turno\": \"rojo\", ")
                .append("\"pista_actual\": \"TEST\", ")
                .append("\"numero_pista\": 1, ")
                .append("\"aciertos_rojo\": 0, ")
                .append("\"aciertos_azul\": 0, ")
                .append("\"equipo_ganador\": \"\", ")
                .append("\"cartas\": [");

        for (int i = 0; i < 25; i++) {
            sb.append("{")
                    .append("\"id\": ").append(i).append(", ")
                    .append("\"id_carta\": ").append(i).append(", ")
                    .append("\"palabra\": \"P").append(i).append("\", ")
                    .append("\"revelada\": false, ")
                    .append("\"tipo\": \"civil\", ")
                    .append("\"color\": \"blanco\", ")
                    .append("\"imagen_url\": \"\"")
                    .append("}");
            if (i < 24) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static Matcher<View> hasChildCount(final int count) {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description d) { d.appendText("con " + count + " hijos"); }
            @Override protected boolean matchesSafely(View v) {
                return (v instanceof GridLayout) && ((GridLayout) v).getChildCount() == count;
            }
        };
    }
}
