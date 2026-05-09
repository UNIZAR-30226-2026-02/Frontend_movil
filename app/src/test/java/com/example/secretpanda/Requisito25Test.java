package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.endMatch.FinPartidaActivity;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito25Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test(timeout = 10000)
    public void testMostrarGanadorYAciertosAlFinalizarPartida() throws Exception {
        // Configuramos el servidor para devolver unos resultados específicos
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Petición de las estadísticas finales (/partida/{id}/fin)
                if (path.contains("/fin")) {
                    String jsonFin = "{" +
                            "\"equipo_ganador\": \"Azul\", " +
                            "\"aciertos_rojo\": 6, " +
                            "\"aciertos_azul\": 8" +
                            "}";
                    return new MockResponse().setResponseCode(200).setBody(jsonFin);
                }

                // Petición del rol del jugador
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"equipo\":\"rojo\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FinPartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 25);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaJugador");

        try (ActivityScenario<FinPartidaActivity> scenario = ActivityScenario.launch(intent)) {
            // Damos tiempo a OkHttp para que haga las peticiones y a la UI para que las pinte
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();


            // El equipo ganador aparece en pantalla.
            // Dado que somos rojos y ganaron los azules, tv_detalle_victoria nos lo indica.
            onView(withId(R.id.tv_detalle_victoria))
                    .check(matches(withText(containsString("Victoria para el Equipo Azul"))));

            // Se muestran los aciertos del equipo ROJO (Enviamos un 6)
            onView(withId(R.id.tv_aciertos_rojo))
                    .check(matches(withText("6")));

            // Se muestran los aciertos del equipo AZUL (Enviamos un 8)
            onView(withId(R.id.tv_aciertos_azul))
                    .check(matches(withText("8")));
        }
    }
}