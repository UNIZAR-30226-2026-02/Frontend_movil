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
public class Requisito30Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        // Inyectamos el Token para que las peticiones se autoricen
        TokenManager tm = new TokenManager(ApplicationProvider.getApplicationContext());
        tm.saveToken("token_falso_para_busqueda");

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
    public void testBuscarAmigoPorTag() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                // Simulación de la carga inicial de la pantalla
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("GET")) {
                    return new MockResponse().setResponseCode(200).setBody("[]");
                }

                // Simulación de la BÚSQUEDA por tag en el servidor
                if (path.contains("/amigos/solicitudes") && request.getMethod().equals("POST")) {
                    // El servidor encuentra al usuario buscado
                    return new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SolicitudesActivity.class);

        try (ActivityScenario<SolicitudesActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // USO DE LA BARRA DE BÚSQUEDA

            // Nos aseguramos de estar en la pestaña correcta (layout_enviar_solicitud)
            onView(withId(R.id.tab_enviar)).perform(click());
            ShadowLooper.idleMainLooper();

            // Usamos tu barra de búsqueda (input_nombre_solicitud) para buscar el Tag
            onView(withId(R.id.input_nombre_solicitud))
                    .perform(typeText("PandaBuscado"), closeSoftKeyboard());

            // Ejecutamos la acción de búsqueda
            onView(withId(R.id.btn_enviar_solicitud))
                    .perform(click());

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // 4. Verificamos que el sistema procesa la búsqueda correctamente y muestra el feedback
            onView(withId(R.id.txt_feedback_solicitud))
                    .check(matches(isDisplayed()));
        }
    }
}