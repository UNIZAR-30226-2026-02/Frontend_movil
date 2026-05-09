package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

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
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito23_1Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path != null) {
                    if (path.contains("/lobby")) {
                        String mockLobbyJson = "{" +
                                "\"es_publica\": true," +
                                "\"codigo_partida\": \"ABCD\"," +
                                "\"tiempo_espera\": 60," +
                                "\"max_jugadores\": 8," +
                                "\"jugadores\": []" +
                                "}";
                        return new MockResponse().setResponseCode(200).setBody(mockLobbyJson);
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testExistenEquiposRojoYAzul() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 999);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007");

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Contadores de equipos en la cabecera

            onView(withId(R.id.tv_contador_azul))
                    .check(matches(withEffectiveVisibility(VISIBLE)))
                    .check(matches(withText(containsString("AZUL"))));

            onView(withId(R.id.tv_contador_rojo))
                    .check(matches(withEffectiveVisibility(VISIBLE)))
                    .check(matches(withText(containsString("ROJO"))));

            // Opciones en el selector de equipos

            onView(withId(R.id.btn_gestionar_equipo)).perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            onView(withId(R.id.btn_unirse_azul_popup))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            onView(withId(R.id.btn_unirse_rojo_popup))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
    }
}