package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.endMatch.FinPartidaActivity;
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

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito24_2Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        Intents.init(); // Iniciamos el rastreador de pantallas
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
        mockWebServer.shutdown();
    }

    @Test(timeout = 15000)
    public void testSistemaDetectaFinDePartida_Y_NavegaAFinPartida() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Petición inicial del Rol
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"AZUL\"}");
                }

                // Petición de personalizaciones visuales
                if (path.contains("/jugadores")) {
                    return new MockResponse().setResponseCode(200).setBody("{}");
                }

                // Petición del estado de la Partida
                if (path.contains("/estado")) {
                    String jsonEstado = "{\"estado\": \"finalizada\"}";
                    return new MockResponse().setResponseCode(200).setBody(jsonEstado);
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 242);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaDetective");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            // Damos tiempo a que se resuelvan las peticiones HTTP del onCreate()
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            intended(hasComponent(FinPartidaActivity.class.getName()));
        }
    }
}