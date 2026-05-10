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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito24Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();

        // Configuramos el Dispatcher para simular la carga inicial del Lobby
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path != null) {
                    if (path.contains("/lobby")) {
                        // Simulamos que al entrar, el jugador "Agente007" está inicialmente en el equipo AZUL
                        String mockLobbyJson = "{" +
                                "\"es_publica\": true," +
                                "\"codigo_partida\": \"ABCD\"," +
                                "\"tiempo_espera\": 60," +
                                "\"max_jugadores\": 8," +
                                "\"jugadores\": [" +
                                "    {" +
                                "        \"jugador\": {\"tag\": \"Agente007\", \"foto_perfil\": \"1\"}," +
                                "        \"equipo\": \"AZUL\"" +
                                "    }" +
                                "]}";
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
    public void testSeleccionManualDeEquipoEnLobby() throws Exception {
        // Iniciamos la Sala de Espera (Lobby)
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 999);
        intent.putExtra("MI_NOMBRE_USUARIO", "Agente007");
        intent.putExtra("ES_LIDER", false); // Da igual si es líder o no para elegir equipo

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {

            // Esperamos a que la red cargue el lobby inicial (jugador en el equipo azul)
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();


            // Selección manual de equipo

            // A) El usuario hace clic en el botón de cambiar equipo (el icono de las dos personitas)
            onView(withId(R.id.btn_gestionar_equipo)).perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // B) Verificamos que se abre el diálogo de selección de equipo
            // y que el botón de unirse al equipo ROJO es visible y clickeable
            onView(withId(R.id.btn_unirse_rojo_popup))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click()); // El usuario selecciona manualmente el equipo rojo

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // C) Verificamos que la acción se ha completado con éxito
            onView(withId(R.id.btn_unirse_rojo_popup)).check(doesNotExist());
        }
    }
}