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

import okhttp3.Dispatcher;
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
public class Requisito31Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
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
    public void testAceptarSolicitudDeAmistad() throws Exception {
        mockWebServer.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Simular la petición GET que trae las solicitudes recibidas
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("GET")) {
                    String jsonSolicitudes = "[" +
                            "{" +
                            "\"id_solicitante\": \"12345\", " +
                            "\"tag_solicitante\": \"NinjaPanda\", " +
                            "\"foto_perfil_solicitante\": \"panda_explorador\", " +
                            "\"fecha_solicitud\": \"2026-05-09\", " +
                            "\"estado\": \"pendiente\"" +
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonSolicitudes);
                }

                // 2. Simular la respuesta del PUT cuando aceptamos la solicitud
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

            // Cambiamos a la pestaña de "RECIBIDAS"
            onView(withId(R.id.tab_recibidas)).perform(click());

            // LE DAMOS TIEMPO AL HILO DE OKHTTP PARA TRAER LA LISTA
            Thread.sleep(500);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Verificamos que nuestro amigo "NinjaPanda" aparece en la lista
            onView(withText("NinjaPanda")).check(matches(isDisplayed()));

            // ACEPTAR LA SOLICITUD
            // Pulsamos en el botón de aceptar (check de color verde) de esa fila
            onView(withId(R.id.btn_aceptar_solicitud)).perform(click());


            Thread.sleep(500);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // VERIFICACIÓN FINAL
            onView(withId(R.id.texto_mensaje_recibidas))
                    .check(matches(isDisplayed()))
                    .check(matches(withText(containsString("aceptada"))));
        }
    }
}