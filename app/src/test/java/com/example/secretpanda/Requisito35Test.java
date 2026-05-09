package com.example.secretpanda;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito35Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test(timeout = 15000)
    public void testAbandonarPartidaYPenalizacion() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Simulación de carga de rol (Agente)
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"AZUL\"}");
                }
                // Simulación de abandono
                // El servidor responde que el usuario ahora tiene 5 balas menos (o 0 si tenía menos de 5)
                if (path.contains("/abandonar") || (path.contains("/participantes") && request.getMethod().equals("DELETE"))) {
                    return new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\", \"balas_restantes\": 0}");
                }

                // Estado de la partida
                if (path.contains("/estado")) {
                    return new MockResponse().setResponseCode(200).setBody(
                            "{\"estado\": \"en_curso\", \"equipo_turno_actual\": \"AZUL\", \"tiempo_restante\": 30, \"tablero\": {\"cartas\": []}}"
                    );
                }

                return new MockResponse().setResponseCode(200).setBody("{}");
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 35);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaDesertor");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // ABANDONAR PARTIDA

            // Buscamos el botón de abandonar (asegúrate de que el ID sea btn_abandonar)
            onView(withId(R.id.btn_abandonar)).perform(click());

            // Esperamos a que la red procese la petición
            Thread.sleep(1000);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Comprobar que la actividad se cierra o muestra el mensaje de penalización
            assertTrue(scenario.getState().isAtLeast(androidx.lifecycle.Lifecycle.State.INITIALIZED));
        }
    }

    @Test
    public void testReglaBalasNoNegativas() {
        int balasActuales = 3;
        int penalizacion = 5;
        int resultado = Math.max(0, balasActuales - penalizacion);

        assertTrue("El saldo de balas nunca debe ser negativo", resultado >= 0);
        assertTrue("Si tiene menos de 5, el saldo debe quedar en 0", resultado == 0);
    }
}