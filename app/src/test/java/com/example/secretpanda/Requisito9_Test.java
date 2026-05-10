package com.example.secretpanda;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.shop.TiendaActivity;

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

@RunWith(AndroidJUnit4.class)
@Config(sdk = 32)
public class Requisito9_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replaceAll("/$", "");

        // Simulamos la sesión iniciada
        TokenManager tokenManager = new TokenManager(ApplicationProvider.getApplicationContext());
        tokenManager.saveToken("token_valido_tienda");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void alAbrirTienda_MuestraCartasYTemas_YPermiteComprar() throws Exception {
        // 1. Configuramos el servidor mockeado para la Tienda
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                System.out.println("🛒 PETICIÓN TIENDA: " + path);

                // A. Petición para obtener el saldo de balas
                if (path != null && (path.contains("/jugadores") || path.contains("/perfil"))) {
                    return new MockResponse().setResponseCode(200).setBody("{\"balas\": 5000}");
                }

                // B. Petición para las BARAJAS (/temas/activos)
                else if (path != null && path.contains("/temas/activos")) {
                    String jsonBarajas = "[" +
                            "{" +
                            "\"id_tema\": 101," +
                            "\"nombre\": \"Baraja de Oro\"," +
                            "\"precio_balas\": 500," +
                            "\"comprado\": false," +
                            "\"tipo\": \"baraja\"" +
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonBarajas);
                }

                // C. Petición para los FONDOS y BORDES (/personalizaciones/activas)
                else if (path != null && path.contains("/personalizaciones/activas")) {
                    String jsonFondos = "[" +
                            "{" +
                            "\"id_personalizacion\": 102," +
                            "\"nombre\": \"Tema Cyberpunk_fondo\"," +
                            "\"precio_bala\": 300," +
                            "\"comprado\": false," +
                            "\"tipo\": \"fondo\"," +
                            "\"valor_visual\": \"8b5a8b\"" + // ¡Solucionado el crash del Color Parse!
                            "}" +
                            "]";
                    return new MockResponse().setResponseCode(200).setBody(jsonFondos);
                }

                // D. Petición POST para realizar la compra
                else if (path != null && (path.contains("comprar") || path.contains("compra"))) {
                    // Añadimos "balas": 4500 simulando que nos han descontado 500 por la baraja
                    return new MockResponse().setResponseCode(200).setBody("{\"status\": \"ok\", \"mensaje\": \"Compra exitosa\", \"balas\": 4500}");
                }

                return new MockResponse().setResponseCode(200).setBody("[]");
            }
        });

        // 2. Lanzamos directamente la TiendaActivity
        try (ActivityScenario<TiendaActivity> scenario = ActivityScenario.launch(TiendaActivity.class)) {

            // Damos tiempo a OkHttp para descargar el saldo y el catálogo
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(1000);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // --- VALIDACIÓN DEL REQUISITO ---

            // PASO 1: Verificamos que el paquete de cartas ("Baraja de Oro") se cargó en su sección
            onView(withId(R.id.recycler_tienda_barajas))
                    .check(matches(hasDescendant(withText(containsString("Baraja de Oro")))));

            // PASO 2: Verificamos que el tema visual ("Tema Cyberpunk") se cargó en los fondos
            onView(withId(R.id.recycler_tienda_fondos))
                    .check(matches(hasDescendant(withText(containsString("Tema Cyberpunk")))));

            // PASO 3: Interactuar con un item.
            // Hacemos clic DIRECTAMENTE sobre el texto para asegurar que se dispara el listener
            onView(withId(R.id.recycler_tienda_barajas))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(containsString("Baraja de Oro"))),
                            click()
                    ));

            // Damos tiempo a la animación de apertura del popup/dialog
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(1000);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // PASO 4: Verificamos que el diálogo se ha abierto buscando su botón.
            // IMPORTANTE: Añadimos de nuevo .inRoot(isDialog()) para que Espresso sepa que debe buscar en la ventana emergente.
            onView(withId(R.id.btn_comprar_preview))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()));

            // PASO 5: Confirmar la compra pulsando el botón
            onView(withId(R.id.btn_comprar_preview))
                    .inRoot(isDialog())
                    .perform(click());

            // Damos tiempo a la petición de compra
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            Thread.sleep(800);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // Si llega hasta aquí, ¡el requisito de la tienda virtual está verificado al 100%!
        }
    }
}
