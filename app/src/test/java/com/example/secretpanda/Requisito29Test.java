package com.example.secretpanda;

import android.content.Intent;
import android.widget.TextView;

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

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito29Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        TokenManager tm = new TokenManager(ApplicationProvider.getApplicationContext());
        tm.saveToken("token_falso_super_secreto");

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {
        new TokenManager(ApplicationProvider.getApplicationContext()).clearToken();
        mockWebServer.shutdown();
    }

    @Test(timeout = 15000)
    public void testEnviarSolicitudDeAmistad() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Simulación de la carga inicial (la App hace un GET por defecto de recibidas)
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("GET")) {
                    return new MockResponse().setResponseCode(200).setBody("[]");
                }

                // Simulación de enviar solicitud (POST /amigos/solicitudes)
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("POST")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SolicitudesActivity.class);

        try (ActivityScenario<SolicitudesActivity> scenario = ActivityScenario.launch(intent)) {
            // Damos tiempo a la carga inicial del GET
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // ENVIAR SOLICITUD
            onView(withId(R.id.input_nombre_solicitud))
                    .perform(typeText("NinjaPanda"), closeSoftKeyboard());

            // Pulsa el botón de enviar
            onView(withId(R.id.btn_enviar_solicitud)).perform(click());

            // Bucle de espera a que OkHttp termine en segundo plano y actualice la UI
            boolean solicitudEnviada = false;
            for (int i = 0; i < 50; i++) {
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                ShadowLooper.idleMainLooper();

                final String[] textoActual = {""};
                scenario.onActivity(activity -> {
                    TextView tv = activity.findViewById(R.id.txt_feedback_solicitud);
                    if (tv != null) textoActual[0] = tv.getText().toString();
                });

                // Si ya no pone "Enviando..." o pone el mensaje de éxito, salimos del bucle
                if (textoActual[0].contains("con éxito")) {
                    solicitudEnviada = true;
                    break;
                }
                Thread.sleep(100); // Pequeña pausa real para dar aire a OkHttp
            }

            // Verificamos de forma oficial con Espresso (si falló el bucle, Espresso nos dará el log exacto)
            onView(withId(R.id.txt_feedback_solicitud))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("¡Solicitud enviada con éxito!")));
        }
    }
}