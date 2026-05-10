package com.example.secretpanda;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.classification.ClasificacionActivity;

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

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito33Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        // Aseguramos que haya un token para que la actividad no aborte
        TokenManager tm = new TokenManager(ApplicationProvider.getApplicationContext());
        tm.saveToken("token_valido_33");

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // Configuramos la URL para que apunte a nuestro servidor de prueba
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test(timeout = 15000)
    public void testAccesoClasificacionGlobalYAmigos() throws Exception {
        // Configuramos el servidor para que responda con datos de prueba
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String jsonResponse = "[{\"tag\": \"PandaPro\", \"victorias\": 10, \"num_aciertos\": 50}]";
                return new MockResponse().setResponseCode(200).setBody(jsonResponse);
            }
        });

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClasificacionActivity.class);

        try (ActivityScenario<ClasificacionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tab_global)).check(matches(isDisplayed()));
            onView(withId(R.id.tab_amigos)).check(matches(isDisplayed()));

            // LEADERBOARD GLOBAL
            onView(withId(R.id.tab_global)).perform(click());
            // Esperamos un máximo de 3 segundos a que la lista se llene (child-count > 0)
            esperarAQueCargueLaLista(scenario);
            // Verificamos que el dato del servidor aparece en la lista
            onView(withText("PandaPro")).check(matches(isDisplayed()));

            // LEADERBOARD DE AMIGOS
            onView(withId(R.id.tab_amigos)).perform(click());

            // Esperamos de nuevo a que cargue tras el click
            esperarAQueCargueLaLista(scenario);

            // Si llegamos aquí sin error, el sistema permite acceder a ambos leaderboards
            onView(withId(R.id.lista_clasificacion)).check(matches(isDisplayed()));
        }
    }

    /**
     * Espera a que el RecyclerView tenga elementos
     * sin usar métodos internos de tus clases.
     */
    private void esperarAQueCargueLaLista(ActivityScenario<ClasificacionActivity> scenario) throws InterruptedException {
        for (int i = 0; i < 30; i++) { // Reintenta durante 3 segundos
            Thread.sleep(100);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            final boolean[] tieneDatos = {false};
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.lista_clasificacion);
                if (rv != null && rv.getAdapter() != null && rv.getAdapter().getItemCount() > 0) {
                    tieneDatos[0] = true;
                }
            });

            if (tieneDatos[0]) return;
        }
    }
}