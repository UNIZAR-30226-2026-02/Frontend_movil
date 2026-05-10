package com.example.secretpanda;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.join.MisionPublicaActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class Requisito15_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
        String mockUrl = mockWebServer.url("/").toString();
        NetworkConfig.BASE_URL = mockUrl.substring(0, mockUrl.length() - 1);
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testListarYUnirseAPartidaPublica_VerificaRequisitos() throws InterruptedException {
        mockWebServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // 1. TEMAS: Tu Adapter espera una lista de Strings, no objetos.
                // Al incluir "Clásico", nos aseguramos de que pase cualquier filtro.
                if (path != null && path.contains("/jugadores/temas")) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("[\"Clásico\", \"Básico\"]");
                }

                // 2. PARTIDAS PÚBLICAS:
                // CRÍTICO: En tu Partida.java tienes @SerializedName("nombre") para la variable 'tematica'.
                // Y en tu Adapter comparas si tematica es "Clásico".
                if (path != null && (path.contains("/partidas/publicas"))) {
                    if (request.getMethod().equals("GET")) {
                        return new MockResponse().setResponseCode(200)
                                .setBody("[{" +
                                        "\"id_partida\": 105," +
                                        "\"codigo_partida\": \"PUB-105\"," +
                                        "\"tag\": \"Líder de Prueba\"," + // Se mapea a 'nombre' en Java
                                        "\"nombre\": \"Clásico\"," +     // Se mapea a 'tematica' en Java (¡IMPORTANTE!)
                                        "\"max_jugadores\": 8," +
                                        "\"jugadores_actuales\": 3," +
                                        "\"tiempo_espera\": 60," +
                                        "\"estado\": \"esperando\"," +
                                        "\"es_publica\": true" +
                                        "}]");
                    }
                }

                // 3. UNIRSE: La petición POST que el test está esperando detectar.
                if (path != null && (path.contains("/unirse") || path.contains("/ingresar"))) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("{\"id_partida\": 105, \"rol\": \"espia\", \"equipo\": \"rojo\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MisionPublicaActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "AgentePublico");

        try (ActivityScenario<MisionPublicaActivity> scenario = ActivityScenario.launch(intent)) {

            // Inyectamos el token antes de que la Activity haga las llamadas
            scenario.onActivity(activity -> {
                Context ctx = activity.getApplicationContext();
                ctx.getSharedPreferences("SecretPandaPrefs", Context.MODE_PRIVATE)
                        .edit().putString("jwt_token", "token_valido_publico").commit();
            });

            // Esperar a que el RecyclerView se llene
            for (int i = 0; i < 20; i++) {
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                Thread.sleep(100);
            }

            // Simular el CLIC
            scenario.onActivity(activity -> {
                try {
                    RecyclerView rv = activity.findViewById(R.id.rv_misiones);
                    if (rv.getAdapter() != null && rv.getAdapter().getItemCount() > 0) {
                        // Obtenemos el primer elemento
                        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(0);
                        if (holder != null) {
                            View itemView = holder.itemView;

                            // Log de control para ver si el Alpha ya es 1.0 (desbloqueado)
                            System.out.println("DEBUG: Alpha final = " + itemView.getAlpha());

                            // Realizamos el click
                            itemView.performClick();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error al interactuar con el RecyclerView", e);
                }
            });

            // Dar tiempo a la ejecución del OnClickListener y la llamada a la API
            for (int i = 0; i < 20; i++) {
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                Thread.sleep(100);
            }

            // --- VERIFICACIÓN FINAL ---
            boolean hizoPeticionUnirse = false;
            int totalPeticiones = mockWebServer.getRequestCount();

            for (int i = 0; i < totalPeticiones; i++) {
                RecordedRequest req = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
                if (req != null && req.getMethod().equals("POST")) {
                    hizoPeticionUnirse = true;
                    break;
                }
            }

            if (!hizoPeticionUnirse) {
                String ultimoToast = org.robolectric.shadows.ShadowToast.getTextOfLatestToast();
                throw new AssertionError("ERROR: El POST no salió.\n" +
                        "Último Toast: " + ultimoToast + "\n" +
                        "Asegúrate de que 'nombre' en el JSON sea 'Clásico' para que el Adapter active el botón.");
            }

            assertTrue("La aplicación nunca envió la petición POST para unirse.", hizoPeticionUnirse);
        }
    }
}