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

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito20Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path != null) {
                    if (path.contains("/jugadores")) {
                        return new MockResponse().setResponseCode(200)
                                .setBody("{\"marco_carta_equipado\":\"\",\"fondo_tablero_equipado\":\"\"}");
                    }
                    else if (path.contains("/rol")) {
                        return new MockResponse().setResponseCode(200)
                                .setBody("{\"rol\":\"agente\", \"equipo\":\"azul\"}");
                    }
                    else if (path.contains("/estado")) {
                        String mockEstadoJson = "{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"azul\", \"fase_turno\":\"votando\", \"pista_actual\":{\"palabra_pista\":\"PLANTA\", \"pista_numero\":1}, \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"oculta\", \"tipo\":\"azul\"}]}}";
                        return new MockResponse().setResponseCode(200).setBody(mockEstadoJson);
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
    public void testAgenteDeCampoPuedeVotarCarta() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 999);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007");
        intent.putExtra("MI_EQUIPO", "AZUL");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Verificar que la pista aparece en pantalla
            onView(withId(R.id.tv_fase_partida)).check(matches(withText("PISTA: PLANTA (1)")));

            // Tocar la carta basada en la pista
            onView(withText("BAMBÚ")).perform(clickEnCarta());

            // Verificar el popup de visualización (Diciéndole a Espresso que mire en el DIÁLOGO)
            onView(withId(R.id.tv_palabra_preview))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .check(matches(withText("BAMBÚ")));

            // Confirmar el voto pulsando el botón "VOTAR" en el DIÁLOGO
            onView(withId(R.id.btn_votar_preview))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            // Al votar, el popup se cierra. Verificamos que volvemos a ver la fase de la partida
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks(); // Esperar a que se cierre
            onView(withId(R.id.tv_fase_partida)).check(matches(isDisplayed()));
        }
    }

    private static ViewAction clickEnCarta() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Fuerza clic en la carta y espera a que la UI dibuje el popup";
            }

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