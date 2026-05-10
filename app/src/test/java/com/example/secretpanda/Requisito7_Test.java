package com.example.secretpanda;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
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

import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.data.TokenManager;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 32)
public class Requisito7_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replaceAll("/$", "");

        TokenManager tokenManager = new TokenManager(ApplicationProvider.getApplicationContext());
        tokenManager.saveToken("token_falso");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void alAbrirHistorialDesdeMenu_MuestraDatosCorrectosDeAgente() throws Exception {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();

                if (path != null && path.contains("/jugadores/historial")) {
                    String jsonHistorial = "[" +
                            "{" +
                            "\"id_partida\":50," +
                            "\"codigo_partida\":\"PANDA-X\"," +
                            "\"fecha_fin\":\"2023-10-27T10:00:00Z\"," +
                            "\"equipo\":\"rojo\"," +
                            "\"rojo_gana\":true," +
                            "\"rol\":\"agente\"," +
                            "\"num_aciertos\":5," +
                            "\"num_fallos\":2" +
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonHistorial);
                }
                // Evitamos el JSONException devolviendo objetos genéricos válidos para otras llamadas
                else if (path != null && path.contains("/jugadores")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"tag\":\"PandaOriginal\"}");
                }

                return new MockResponse().setResponseCode(200).setBody("{}");
            }
        });

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Navegación
            onView(withId(R.id.btn_menu_opciones)).perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            onView(withId(R.id.btn_opcion_historial))
                    .inRoot(isPlatformPopup())
                    .perform(click());

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(800);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Verificamos que se abre el diálogo
            onView(withText("Historial"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // EN LUGAR DE VALIDAR EL RECYCLERVIEW COMPLETO, VALIDAMOS LOS TEXTVIEWS INDIVIDUALES.
            // Es mucho más seguro y exacto.

            // Verificamos el texto de aciertos/fallos (El Adapter concatena: "ACIERTOS: 5 | FALLOS: 2")
            onView(withId(R.id.item_historial_aciertos))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed())) // Verifica que se hizo visible (dejó de ser "gone")
                    .check(matches(withText(containsString("ACIERTOS: 5"))))
                    .check(matches(withText(containsString("FALLOS: 2"))));

            // Verificamos que el rol "agente" aparece en la UI (en R.id.item_historial_rol)
            onView(withId(R.id.item_historial_rol))
                    .inRoot(isDialog())
                    .check(matches(withText(containsString("AGENTE"))));

        }
    }
}
