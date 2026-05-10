package com.example.secretpanda;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.match.PartidaActivity;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class Requisito36Test {

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

    @Test(timeout = 20000)
    public void testFiltroAutomaticoPalabrasOfensivas() throws Exception {
        // Mocks para que la partida cargue sin errores
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"rol\":\"agente\", \"equipo\":\"AZUL\"}"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"estado\": \"en_curso\", \"tablero\": {\"cartas\": []}}"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 36);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaTest");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            // Esperamos a que la UI se estabilice
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // INYECTAMOS EL MENSAJE FILTRADO
            scenario.onActivity(activity -> {
                try {
                    // Accedemos a tu lista privada 'historialChat' mediante reflexión
                    java.lang.reflect.Field field = activity.getClass().getDeclaredField("historialChat");
                    field.setAccessible(true);
                    List<JSONObject> historial = (List<JSONObject>) field.get(activity);

                    // Creamos el mensaje que simula haber sido bloqueado por el backend
                    JSONObject mensajeBloqueado = new JSONObject();
                    mensajeBloqueado.put("tag", "Sistema");
                    mensajeBloqueado.put("mensaje", "palabrota");
                    mensajeBloqueado.put("es_valido", false);

                    historial.add(mensajeBloqueado);

                    // Abrimos el chat (esto disparará 'mostrarDialogoChat' y leerá el historial)
                    activity.findViewById(R.id.btn_chat).performClick();
                } catch (Exception e) {
                    throw new RuntimeException("Error inyectando datos en el test", e);
                }
            });

            // Dejamos que Robolectric procese la apertura del Diálogo
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Comprobamos que el texto de censura aparece en el diálogo
            // Usamos .inRoot(isDialog()) porque el chat es un Diálogo, no la Activity principal
            onView(withText(containsString("Mensaje bloqueado")))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // Verificamos que el contenedor de mensajes también está ahí por seguridad
            onView(withId(R.id.contenedor_mensajes))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
    }
}