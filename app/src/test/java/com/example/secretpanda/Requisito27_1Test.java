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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito27_1Test {

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
    public void testAgentePuedeUsarChatFueraDeSuTurno() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Configuramos nuestra identidad: Agente del equipo AZUL
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"AZUL\"}");
                }

                // Configuramos el estado: Es el turno del equipo ROJO y fase JEFE_PISTA (para que pinte ESPERANDO)
                if (path.contains("/estado")) {
                    String jsonEstado = "{\"estado\": \"en_curso\", \"equipo_turno_actual\": \"ROJO\", \"fase_turno\": \"JEFE_PISTA\", \"tablero\": {\"cartas\": []}}";
                    return new MockResponse().setResponseCode(200).setBody(jsonEstado);
                }

                if (path.contains("/jugadores")) {
                    return new MockResponse().setResponseCode(200).setBody("{}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 271);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaInfiltrado");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Comprobamos que efectivamente no es nuestro turno.
            onView(withId(R.id.tv_fase_partida)).check(matches(withText(containsString("ROJO"))));

            // Abrimos el chat
            onView(withId(R.id.btn_chat)).perform(click());
            ShadowLooper.idleMainLooper();

            // La zona de escribir está visible para nosotros (Agentes)
            onView(withId(R.id.zona_escribir))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // Simulamos que escribimos un mensaje a nuestro equipo
            onView(withId(R.id.input_mensaje))
                    .inRoot(isDialog())
                    .perform(typeText("Cuidado con la carta de la esquina"), closeSoftKeyboard());

            // Le damos a enviar
            onView(withId(R.id.btn_enviar_mensaje))
                    .inRoot(isDialog())
                    .perform(click());
            ShadowLooper.idleMainLooper();

            // Verificamos que el input se vació al enviarse
            onView(withId(R.id.input_mensaje))
                    .inRoot(isDialog())
                    .check(matches(withText("")));
        }
    }
}