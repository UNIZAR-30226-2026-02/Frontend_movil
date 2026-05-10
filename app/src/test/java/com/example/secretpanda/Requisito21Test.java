package com.example.secretpanda;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito21Test {

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
                        return new MockResponse().setResponseCode(200).setBody("{}");
                    }
                    else if (path.contains("/rol")) {
                        return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"azul\"}");
                    }
                    else if (path.contains("/estado")) {
                        String estadoConVotos = "{\"estado\":\"en_curso\", \"equipo_turno_actual\":\"azul\", \"fase_turno\":\"votando\", \"pista_actual\":{\"palabra_pista\":\"PLANTA\", \"pista_numero\":1}, \"tablero\":{\"cartas\":[{\"id_carta_tablero\":1, \"palabra\":\"BAMBÚ\", \"estado\":\"oculta\", \"tipo\":\"azul\"}]}, \"votos_turno_actual\":[{\"id_carta_tablero\":1, \"tag\":\"AgenteSecretoPro\"}]}";
                        return new MockResponse().setResponseCode(200).setBody(estadoConVotos);
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
    public void testContabilizacionDeVotosEnTiempoReal() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 999);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007"); // Nuestro usuario
        intent.putExtra("MI_EQUIPO", "AZUL");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {

            // Esperamos a que la red cargue el estado (el MockJSON con el voto del compañero)
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            int colorVotoCompanero = Color.parseColor("#FFC107");

            onView(withText("BAMBÚ"))
                    .check(matches(isDisplayed()))
                    .check(matches(isDescendantOfA(hasBackgroundColor(colorVotoCompanero))));
        }
    }

    private static Matcher<View> hasBackgroundColor(final int expectedColor) {
        return new BoundedMatcher<View, View>(View.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Debería tener el color de fondo: " + expectedColor);
            }

            @Override
            protected boolean matchesSafely(View view) {
                Drawable background = view.getBackground();
                if (background instanceof ColorDrawable) {
                    return ((ColorDrawable) background).getColor() == expectedColor;
                }
                return false;
            }
        };
    }
}