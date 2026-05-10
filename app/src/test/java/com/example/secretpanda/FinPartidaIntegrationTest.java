package com.example.secretpanda;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.endMatch.FinPartidaActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class FinPartidaIntegrationTest {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
        NetworkConfig.BASE_URL = "http://localhost:8080/";
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testRecompensa_PartidaGanada_MuestraGanado() {
        // Configuramos respuestas: Usuario ROJO, Gana ROJO -> GANA
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/fin")) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("{\"equipo_ganador\": \"rojo\", \"aciertos_rojo\": 10, \"aciertos_azul\": 2}");
                } else if (request.getPath().contains("/rol")) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("{\"equipo\": \"rojo\"}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        lanzarActividad();
        onView(withId(R.id.tv_titulo_victoria)).check(matches(withText("HAS GANADO")));
    }

    @Test
    public void testRecompensa_PartidaPerdida_MuestraPerdido() {
        // Configuramos el servidor para el escenario de DERROTA
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                // Si la actividad pide los resultados de la partida
                if (request.getPath().contains("/fin")) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("{\"equipo_ganador\": \"azul\", \"aciertos_rojo\": 2, \"aciertos_azul\": 10}");
                }
                // Si la actividad pide el rol del jugador
                else if (request.getPath().contains("/rol")) {
                    return new MockResponse().setResponseCode(200)
                            .setBody("{\"equipo\": \"rojo\"}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // Lanzamos la actividad
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FinPartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 123);
        intent.putExtra("MI_NOMBRE_USUARIO", "UsuarioTest");
        ActivityScenario.launch(intent);

        // Procesamos hilos asíncronos
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // VALIDACIÓN: Rojo (jugador) vs Azul (ganador) -> "HAS PERDIDO"
        onView(withId(R.id.tv_titulo_victoria))
                .check(matches(withText("HAS PERDIDO")));
    }

    private void lanzarActividad() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FinPartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 123);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007");

        ActivityScenario.launch(intent);

        // Forzamos a Robolectric a ejecutar los hilos de red y los runOnUiThread
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }
}
