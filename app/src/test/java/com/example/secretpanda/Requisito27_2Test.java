package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.Field;
import java.util.List;

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
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito27_2Test {

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
    public void testJefePuedeLeerChatPeroNoEscribir() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Configuramos nuestra identidad como Jefe (lider) del equipo AZUL
                if (path.contains("/participantes/rol")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"rol\":\"lider\", \"equipo\":\"AZUL\"}");
                }

                // Estado de la partida normal
                if (path.contains("/estado")) {
                    String jsonEstado = "{\"estado\": \"en_curso\", \"tablero\": {\"cartas\": []}}";
                    return new MockResponse().setResponseCode(200).setBody(jsonEstado);
                }

                if (path.contains("/jugadores")) {
                    return new MockResponse().setResponseCode(200).setBody("{}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 272);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaJefe");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Inyectamos un mensaje falso en el historial del chat para simular que
            // un Agente de nuestro equipo (AZUL) ha escrito algo por el WebSocket.
            scenario.onActivity(activity -> {
                try {
                    Field historialField = activity.getClass().getDeclaredField("historialChat");
                    historialField.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    List<JSONObject> historial = (List<JSONObject>) historialField.get(activity);

                    JSONObject mensajeSimulado = new JSONObject();
                    mensajeSimulado.put("tag", "AgenteAzul_007");
                    mensajeSimulado.put("mensaje", "Hola Jefe, ¿qué carta elegimos?");
                    mensajeSimulado.put("es_valido", true);

                    historial.add(mensajeSimulado);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            ShadowLooper.idleMainLooper();

            // Abrimos el menú del chat
            onView(withId(R.id.btn_chat)).perform(click());
            ShadowLooper.idleMainLooper();


            // El Jefe puede leer los mensajes de su equipo.
            onView(withText("Hola Jefe, ¿qué carta elegimos?"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // El Jefe no puede escribir en el chat.
            onView(withId(R.id.zona_escribir))
                    .inRoot(isDialog())
                    .check(matches(not(isDisplayed())));
        }
    }
}