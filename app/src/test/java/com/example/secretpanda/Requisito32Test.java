package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.SolicitudesActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

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
@Config(sdk = 34)
public class Requisito32Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        // Inyectamos un Token falso para permitir la comunicación HTTP
        TokenManager tm = new TokenManager(ApplicationProvider.getApplicationContext());
        tm.saveToken("token_falso_para_solicitudes");

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {
        new TokenManager(ApplicationProvider.getApplicationContext()).clearToken();
        mockWebServer.shutdown();
    }

    @Test(timeout = 10000)
    public void testRechazarSolicitudDeAmistad() throws Exception {
        mockWebServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Petición inicial para cargar la lista
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("GET")) {
                    String jsonSolicitudes = "[" +
                            "{" +
                            "\"id_solicitante\": \"99999\", " +
                            "\"tag_solicitante\": \"EnemigoPanda\", " +
                            "\"foto_perfil_solicitante\": \"panda_mago\", " +
                            "\"fecha_solicitud\": \"2026-05-09\", " +
                            "\"estado\": \"pendiente\"" +
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonSolicitudes);
                }

                // Respuesta al botón de rechazar (PUT)
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("PUT")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SolicitudesActivity.class);

        try (ActivityScenario<SolicitudesActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Cambiamos a la pestaña de Solicitudes "RECIBIDAS"
            onView(withId(R.id.tab_recibidas)).perform(click());

            // Damos tiempo a OkHttp para cargar la lista
            Thread.sleep(500);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Verificamos que la solicitud de "EnemigoPanda" está en pantalla
            onView(withText("EnemigoPanda")).check(matches(isDisplayed()));

            // RECHAZAR LA SOLICITUD
            onView(withId(R.id.btn_rechazar_solicitud)).perform(click());
            // Damos tiempo a OkHttp para que mande el PUT y reciba la confirmación
            Thread.sleep(500);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Aparece el mensaje de "Solicitud rechazada"
            onView(withId(R.id.texto_mensaje_recibidas))
                    .check(matches(isDisplayed()))
                    .check(matches(withText(containsString("rechazada"))));
        }
    }
}