package com.example.secretpanda;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.hamcrest.Matcher;
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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito22Test {

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

    @Test
    public void testVotoMayoriaRevelaCarta() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            int peticionesEstado = 0;
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path != null) {
                    if (path.contains("/jugadores")) return new MockResponse().setResponseCode(200).setBody("{}");
                    if (path.contains("/rol")) return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"azul\"}");

                    if (path.contains("/estado")) {
                        peticionesEstado++;
                        if (peticionesEstado == 1) {
                            return new MockResponse().setResponseCode(200).setBody("{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"azul\", \"fase_turno\":\"votando\", \"pista_actual\":{\"palabra_pista\":\"PLANTA\", \"pista_numero\":1}, \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"oculta\", \"tipo\":\"azul\"}]}}");
                        } else {
                            // TRAS VOTAR: Carta marcada como REVELADA
                            return new MockResponse().setResponseCode(200).setBody("{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"azul\", \"fase_turno\":\"votando\", \"pista_actual\":{\"palabra_pista\":\"PLANTA\", \"pista_numero\":1}, \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"revelada\", \"tipo\":\"azul\"}]}}");
                        }
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = crearIntentPartida();

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {

            simularVotoYRecarga(scenario);

            // Verificamos con la pantalla aún abierta
            onView(withText("BAMBÚ")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testVotoEmpatePierdeTurno() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            int peticionesEstado = 0;
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path != null) {
                    if (path.contains("/jugadores")) return new MockResponse().setResponseCode(200).setBody("{}");
                    if (path.contains("/rol")) return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"azul\"}");

                    if (path.contains("/estado")) {
                        peticionesEstado++;
                        if (peticionesEstado == 1) {
                            return new MockResponse().setResponseCode(200).setBody("{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"azul\", \"fase_turno\":\"votando\", \"pista_actual\":{\"palabra_pista\":\"PLANTA\", \"pista_numero\":1}, \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"oculta\", \"tipo\":\"azul\"}]}}");
                        } else {
                            return new MockResponse().setResponseCode(200).setBody("{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"rojo\", \"fase_turno\":\"esperando_pista\", \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"oculta\", \"tipo\":\"azul\"}]}}");
                        }
                    }
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = crearIntentPartida();

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {

            simularVotoYRecarga(scenario);

            onView(withId(R.id.tv_fase_partida)).check(matches(withText(containsString("ESPERANDO"))));
        }
    }

    private Intent crearIntentPartida() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 999);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007");
        intent.putExtra("MI_EQUIPO", "AZUL");
        return intent;
    }

    private void simularVotoYRecarga(ActivityScenario<PartidaActivity> scenario) {
        // Carga inicial
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // Tocamos la carta y votamos
        onView(withText("BAMBÚ")).perform(clickEnCarta());
        onView(withId(R.id.btn_votar_preview)).inRoot(isDialog()).perform(click());
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // Forzamos a la Actividad a recrearse (Simular efecto de WebSocket)
        scenario.recreate();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }

    private static ViewAction clickEnCarta() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() { return isDisplayed(); }

            @Override
            public String getDescription() { return "Fuerza clic en la carta"; }

            @Override
            public void perform(UiController uiController, View view) {
                View parent = (View) view.getParent();
                while (parent != null && !parent.isClickable()) {
                    parent = (View) parent.getParent();
                }
                if (parent != null) {
                    parent.performClick();
                    uiController.loopMainThreadUntilIdle();
                }
            }
        };
    }
}