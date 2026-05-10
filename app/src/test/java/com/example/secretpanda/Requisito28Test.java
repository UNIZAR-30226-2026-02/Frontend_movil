package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.PerfilActivity;

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
@Config(sdk = 34)
public class Requisito28Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        // Inyectamos el Token para pasar la validación
        TokenManager tm = new TokenManager(ApplicationProvider.getApplicationContext());
        tm.saveToken("token_falso_para_que_pase_la_verificacion");

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
    public void testVisualizarListaDeAmigosEnPerfil() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.contains("/jugadores")) {
                    String jsonPerfil = "{" +
                            "\"tag\": \"PandaPrincipal\", " +
                            "\"foto_perfil\": \"panda_mago\", " +
                            "\"balas\": 10, " +
                            "\"victorias\": 5, " +
                            "\"derrotas\": 2, " +
                            "\"num_aciertos\": 20, " +
                            "\"num_fallos\": 5" +
                            "}";
                    return new MockResponse().setResponseCode(200).setBody(jsonPerfil);
                }

                if (path.contains("/amigos")) {
                    String jsonAmigos = "[" +
                            "{" +
                            "\"tag\": \"AmigoNinja_007\", " +
                            "\"foto_perfil\": \"panda_explorador\", " +
                            "\"victorias\": 99" +
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonAmigos);
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PerfilActivity.class);
        intent.putExtra("NOMBRE_JUGADOR", "PandaPrincipal");

        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            scenario.onActivity(activity -> {
                activity.findViewById(R.id.tab_amigos).performClick();
            });

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // El tag del amigo debe aparecer en la lista
            onView(withText("AmigoNinja_007")).check(matches(isDisplayed()));

            // Hacemos click en el amigo para abrir el panel de detalles
            onView(withText("AmigoNinja_007")).perform(click());
            ShadowLooper.idleMainLooper();

            // Ahora que el panel de detalles está abierto, verificamos las victorias
            // Usamos el ID exacto que tienes en layout_detalle_amigo
            onView(withId(R.id.stat_amigo_victorias)).check(matches(withText("99")));
        }
    }
}