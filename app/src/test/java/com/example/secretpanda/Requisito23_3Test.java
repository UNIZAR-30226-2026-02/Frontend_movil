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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito23_3Test {

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
    public void testPermitirInicioConEquiposDesequilibrados() throws Exception {
        // escenario: 3 Azules vs 2 Rojos (Desequilibrio permitido)
        // Ambos cumplen el mínimo de 2, por lo que el botón debe estar activo.
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String mockLobbyJson = "{" +
                        "\"max_jugadores\": 8," +
                        "\"jugadores\": [" +
                        "    {\"jugador\": {\"tag\": \"LiderAzul\"}, \"equipo\": \"AZUL\"}," +
                        "    {\"jugador\": {\"tag\": \"AgenteA1\"}, \"equipo\": \"AZUL\"}," +
                        "    {\"jugador\": {\"tag\": \"AgenteA2\"}, \"equipo\": \"AZUL\"}," + // 3 Azules
                        "    {\"jugador\": {\"tag\": \"LiderRojo\"}, \"equipo\": \"ROJO\"}," +
                        "    {\"jugador\": {\"tag\": \"AgenteR1\"}, \"equipo\": \"ROJO\"}" +   // 2 Rojos
                        "]}";
                return new MockResponse().setResponseCode(200).setBody(mockLobbyJson);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 303);
        intent.putExtra("MI_NOMBRE_USUARIO", "LiderAzul");
        intent.putExtra("ES_LIDER", true);

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // El botón debe estar habilitado aunque haya 3 vs 2
            onView(withId(R.id.btn_iniciar_partida_principal))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testPermitirUnirseAEquipoAunqueSeaMasGrande() throws Exception {
        // escenario: Hay 2 Azules y 2 Rojos. Tú eres Rojo.
        // Quieres unirte al equipo Azul (que pasaría a tener 3 vs 1).
        // Aunque 1 no cumple el mínimo para empezar, el sistema debe permitirte cambiar.
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String mockLobbyJson = "{" +
                        "\"max_jugadores\": 8," +
                        "\"jugadores\": [" +
                        "    {\"jugador\": {\"tag\": \"U1\"}, \"equipo\": \"AZUL\"}," +
                        "    {\"jugador\": {\"tag\": \"U2\"}, \"equipo\": \"AZUL\"}," +
                        "    {\"jugador\": {\"tag\": \"U3\"}, \"equipo\": \"ROJO\"}," +
                        "    {\"jugador\": {\"tag\": \"MiUser\"}, \"equipo\": \"ROJO\"}" +
                        "]}";
                return new MockResponse().setResponseCode(200).setBody(mockLobbyJson);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 304);
        intent.putExtra("MI_NOMBRE_USUARIO", "MiUser");

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Abrimos gestión de equipo
            onView(withId(R.id.btn_gestionar_equipo)).perform(androidx.test.espresso.action.ViewActions.click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // VERIFICACIÓN: El botón de unirse al Azul debe estar habilitado
            // aunque el Azul ya tenga más gente (2) que el Rojo (donde quedará solo 1).
            onView(withId(R.id.btn_unirse_azul_popup))
                    .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                    .check(matches(isEnabled()));
        }
    }
}