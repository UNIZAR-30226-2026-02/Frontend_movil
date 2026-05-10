package com.example.secretpanda;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito23Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();

        // Dispatcher básico: HomeActivity pide los datos del jugador (las balas y la foto) al arrancar.
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if (path != null && path.contains("/jugadores")) {
                    return new MockResponse().setResponseCode(200).setBody("{\"balas\": 100, \"foto_perfil\": \"1\"}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        // Para este test no necesitamos WebSocket, pero lo reasignamos por seguridad
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testManualReglasJuegoAccesible() throws Exception {
        // Iniciamos directamente desde el menú principal (HomeActivity)
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {

            // Esperamos a que la red cargue los datos de usuario
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Clic en el botón de opciones
            onView(withId(R.id.btn_menu_opciones)).perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Clic en "Cómo jugar" dentro del menú desplegable
            // Le decimos a Espresso explícitamente que busque en la ventana Popup emergente
            onView(withText("> Cómo jugar"))
                    .inRoot(isPlatformPopup())
                    .check(matches(isDisplayed()))
                    .perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // A) Verificamos que el Diálogo del Manual se ha abierto comprobando el Título
            onView(withId(R.id.titulo))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .check(matches(withText("Manual de Campo")));

            // B) Verificamos que contiene reglas clave del juego (Escaneamos el texto del ScrollView)
            onView(withText(containsString("El Jefe de Espionaje da pistas de una sola palabra")))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            onView(withText(containsString("¡Cuidado con el ASESINO!")))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // El manual se puede cerrar correctamente

            // Cerramos el manual
            onView(withId(R.id.btn_cerrar_popup))
                    .inRoot(isDialog())
                    .perform(click());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Comprobamos que el título del manual ha desaparecido de la pantalla
            onView(withId(R.id.titulo)).check(doesNotExist());
        }
    }
}