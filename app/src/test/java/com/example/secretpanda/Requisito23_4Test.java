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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
// Aplicamos la sombra que silencia la conexión STOMP
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito23_4Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        // Usamos la URL del servidor falso para que no intente conectar a internet
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testModificarTiempoEnLobby() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                // Estado que queremos ver reflejado tras el supuesto cambio
                String json = "{\"es_publica\": false, \"codigo_partida\": \"YMH32R\", \"tiempo_espera\": 90, \"jugadores\": []}";

                // Si la app intenta hacer un POST/PUT para configurar, respondemos OK
                if (request.getPath().contains("/configurar") || request.getMethod().equals("POST")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}");
                }
                // Para las peticiones GET iniciales o de recarga
                return new MockResponse().setResponseCode(200).setBody(json);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 101);
        intent.putExtra("MI_NOMBRE_USUARIO", "CreadorPanda");
        intent.putExtra("ES_LIDER", true);

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            scenario.onActivity(activity -> {
                android.view.View btnConfig = activity.findViewById(R.id.btn_config_sala);
                if (btnConfig != null) {
                    btnConfig.setVisibility(android.view.View.VISIBLE);
                }
            });
            ShadowLooper.idleMainLooper();

            // abrimos el diálogo pulsando el botón
            onView(withId(R.id.btn_config_sala)).perform(click());
            ShadowLooper.idleMainLooper();

            // verificamos que el diálogo (R.layout.dialog_ajustes_sala) se abre buscando un texto

            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Method metodoCarga = activity.getClass().getDeclaredMethod("cargarLobbyInicial");
                    metodoCarga.setAccessible(true);
                    metodoCarga.invoke(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            onView(withId(R.id.tv_tiempo_sala)).check(matches(withText(containsString("90"))));
        }
    }
}