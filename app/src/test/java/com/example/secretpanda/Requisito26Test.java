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
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito26Test {

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

    @Test(timeout = 10000)
    public void testAgentePuedeEscribirEnChat() throws Exception {
        // Configuramos el servidor para decirle a la App que somos un AGENTE
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"AZUL\"}");
                }
                if (path.contains("/jugadores")) return new MockResponse().setResponseCode(200).setBody("{}");
                if (path.contains("/estado")) return new MockResponse().setResponseCode(200).setBody("{\"estado\": \"en_curso\", \"tablero\": {\"cartas\": []}}");
                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 26);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaAgente");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Abrimos el chat
            onView(withId(R.id.btn_chat)).perform(click());
            ShadowLooper.idleMainLooper();

            // Como somos Agentes, la zona de escribir debe estar visible
            onView(withId(R.id.zona_escribir))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
    }

    @Test(timeout = 10000)
    public void testJefeEspionajeNoPuedeEscribirEnChat() throws Exception {
        // Configuramos el servidor para decirle a la App que ahora somos el JEFE (Líder)
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path.contains("/participantes/rol")) {
                    // La app convierte "lider" en Jefe de Espionaje
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"lider\", \"equipo\":\"AZUL\"}");
                }
                if (path.contains("/jugadores")) return new MockResponse().setResponseCode(200).setBody("{}");
                if (path.contains("/estado")) return new MockResponse().setResponseCode(200).setBody("{\"estado\": \"en_curso\", \"tablero\": {\"cartas\": []}}");
                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 26);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaJefe");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Abrimos el chat
            onView(withId(R.id.btn_chat)).perform(click());
            ShadowLooper.idleMainLooper();

            // Como somos Jefes, la zona de escribir debe estar oculta
            onView(withId(R.id.zona_escribir))
                    .inRoot(isDialog())
                    .check(matches(not(isDisplayed())));
        }
    }
}