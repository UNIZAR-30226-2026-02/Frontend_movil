package com.example.secretpanda;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.HomeActivity;

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

import com.example.secretpanda.ui.home.achivements.LogrosActivity;


@RunWith(AndroidJUnit4.class)
@Config(sdk = 32)
public class LogrosYMedallasIntegrationTest {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replaceAll("/$", "");

        // Simulamos que el usuario tiene sesión iniciada para las peticiones
        TokenManager tokenManager = new TokenManager(ApplicationProvider.getApplicationContext());
        tokenManager.saveToken("token_falso_para_test");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void alAbrirExpediente_MuestraLogrosYMedallasConEstadoCorrecto() throws Exception {
        // 1. DISPATCHER: Responde dinámicamente
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                System.out.println("🌐 URL SOLICITADA EN LOGROS: " + path);

                if (path != null && path.contains("/jugadores/logros")) {
                    // JSON creado a medida para las variables de tu LogrosActivity
                    String jsonRecompensas = "[" +
                            // 1. LOGRO COMPLETADO (es_logro: true)
                            "{" +
                            "\"nombre\": \"Primera Sangre\"," +
                            "\"descripcion\": \"Juega tu primera partida\"," +
                            "\"balas_recompensa\": 100," +
                            "\"progreso_actual\": 1," +
                            "\"progreso_max\": 1," +
                            "\"completado\": true," +
                            "\"es_logro\": true" +
                            "}," +
                            // 2. MEDALLA COMPLETADA (es_logro: false)
                            "{" +
                            "\"nombre\": \"Veterano\"," +
                            "\"descripcion\": \"Alcanza 100 victorias\"," +
                            "\"balas_recompensa\": 500," +
                            "\"progreso_actual\": 100," +
                            "\"progreso_max\": 100," +
                            "\"completado\": true," +
                            "\"es_logro\": false" + // <-- Esto la envía a recyclerMedallas
                            "}," +
                            // 3. MEDALLA EN PROGRESO (es_logro: false)
                            "{" +
                            "\"nombre\": \"Inquebrantable\"," +
                            "\"descripcion\": \"Sobrevive a 10 partidas\"," +
                            "\"balas_recompensa\": 200," +
                            "\"progreso_actual\": 4," +
                            "\"progreso_max\": 10," +
                            "\"completado\": false," +
                            "\"es_logro\": false" + // <-- Esto la envía a recyclerMedallas
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonRecompensas);
                }

                return new MockResponse().setResponseCode(200).setBody("[]");
            }
        });

        // 2. ¡EL CAMBIO CLAVE! Arrancamos LogrosActivity DIRECTAMENTE
        try (ActivityScenario<LogrosActivity> scenario = ActivityScenario.launch(LogrosActivity.class)) {

            // Damos tiempo a la Actividad para abrirse y a OkHttp para descargar los JSON
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(1000);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // PASO 1: Verificamos que la pantalla se cargó (El recycler de Logros debe verse)
            onView(withId(R.id.recycler_lista_logros)).check(matches(isDisplayed()));

            // PASO 2: Verificamos que el Logro se inyectó en la lista
            onView(withId(R.id.recycler_lista_logros))
                    .check(matches(hasDescendant(withText(containsString("Primera Sangre")))));

            // PASO 3: Cambiar a la pestaña de MEDALLAS
            onView(withId(R.id.tab_medallas)).perform(click());

            // Pausa para que la animación del Layout termine
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(800);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Verificamos que el Recycler de medallas ahora está visible
            onView(withId(R.id.recycler_lista_medallas)).check(matches(isDisplayed()));

            // PASO 4: Forzar dibujado de la lista
            onView(withId(R.id.recycler_lista_medallas))
                    .perform(RecyclerViewActions.scrollToPosition(0));

            onView(withId(R.id.recycler_lista_medallas))
                    .perform(RecyclerViewActions.scrollToPosition(0));

            // Verificamos la Medalla COMPLETADA ("Veterano")
            onView(withId(R.id.recycler_lista_medallas))
                    .check(matches(hasDescendant(allOf(
                            hasDescendant(withText(containsString("Veterano"))),
                            hasDescendant(allOf(
                                    withId(R.id.icono_completado_medalla),
                                    withEffectiveVisibility(Visibility.VISIBLE)
                            ))
                    ))));

            // PASO 6: ¡HACER SCROLL A LA SEGUNDA MEDALLA! (Posición 1)
            onView(withId(R.id.recycler_lista_medallas))
                    .perform(RecyclerViewActions.scrollToPosition(1));

            // Verificamos la Medalla BLOQUEADA ("Inquebrantable")
            onView(withId(R.id.recycler_lista_medallas))
                    .check(matches(hasDescendant(allOf(
                            hasDescendant(withText(containsString("Inquebrantable"))),
                            hasDescendant(allOf(
                                    withId(R.id.icono_completado_medalla),
                                    withEffectiveVisibility(Visibility.GONE)
                            ))
                    ))));
        }
    }
}
