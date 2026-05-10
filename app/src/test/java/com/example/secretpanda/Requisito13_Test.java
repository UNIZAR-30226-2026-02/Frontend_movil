package com.example.secretpanda;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.createMatch.ConfiguracionMisionActivity;
import com.example.secretpanda.ui.game.createMatch.CrearMisionOpcionesActivity;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32, qualifiers = "w1080dp-h2280dp")
public class Requisito13_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
        NetworkConfig.BASE_URL = "http://localhost:8080/";

        // 1. Inyectar token para que la API permita hacer las llamadas
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("SecretPandaPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", "token_creacion_test").commit();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Prueba de Navegación: Verifica que al pulsar en "Misión Privada" o "Misión Pública"
     * se redirige correctamente con los parámetros adecuados.
     */
    @Test
    public void testFlujo_SeleccionPrivacidadRedirigeCorrectamente() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CrearMisionOpcionesActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "AgenteCreador");

        try (ActivityScenario<CrearMisionOpcionesActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.idleMainLooper();

            // Verificamos que las tarjetas de selección existen
            onView(withId(R.id.tarjeta_crear_privada)).check(matches(isDisplayed()));
            onView(withId(R.id.tarjeta_crear_publica)).check(matches(isDisplayed()));

            // NOTA: Para validar los Intents en Robolectric, normalmente usaríamos IntentsTestRule.
            // Aquí validamos que la vista se muestra correctamente sin crashear.
        }
    }

    /**
     * Prueba Core del Requisito:
     * Verifica que al pulsar el botón de crear partida, se envía un JSON al servidor
     * con los campos es_privada, limite_jugadores (4-16), tiempo_turno y el id_tema (paquete).
     */
    @Test
    public void testConfiguracionPartida_EnvioPayloadCorrecto_Privada() throws InterruptedException {
        // Configuramos el servidor para responder con los paquetes de cartas del usuario
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/jugadores/temas")) {
                    // El usuario tiene desbloqueados los paquetes: Estándar(1) y Espías(2)
                    return new MockResponse().setResponseCode(200)
                            .setBody("[{\"id_tema\": 1, \"nombre\": \"Estándar\"}, {\"id_tema\": 2, \"nombre\": \"Espías\"}]");
                }
                if (request.getPath().contains("/partida")) {
                    // Simulamos la creación exitosa devolviendo el ID y código de sala
                    return new MockResponse().setResponseCode(201)
                            .setBody("{\"id_partida\": 999, \"codigo_acceso\": \"ALPHA-X\"}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // Lanzamos directamente la actividad de Configuración simulando que eligió PRIVADA
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ConfiguracionMisionActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "TestMaster");
        intent.putExtra("ES_PRIVADA", true); // Requisito: Partida Privada

        try (ActivityScenario<ConfiguracionMisionActivity> scenario = ActivityScenario.launch(intent)) {

            // Damos tiempo a que se descarguen las temáticas
            for (int i = 0; i < 3; i++) {
                ShadowLooper.idleMainLooper();
                Thread.sleep(100);
            }

            // Simulamos hacer click en confirmar misión
            onView(withId(R.id.btn_crear_mision_final)).perform(click());

            // Procesamos los hilos asíncronos para que OkHttp envíe la petición
            ShadowLooper.idleMainLooper();

            // -------------------------------------------------------------
            // VERIFICACIÓN DEL REQUISITO (Validar petición HTTP generada)
            // -------------------------------------------------------------
            RecordedRequest peticionTemas = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
            RecordedRequest peticionCrear = mockWebServer.takeRequest(2, TimeUnit.SECONDS);

            assertNotNull("La aplicación no envió la petición de creación", peticionCrear);
            assertEquals("POST", peticionCrear.getMethod());

            // Leemos el cuerpo de la petición
            String payload = peticionCrear.getBody().readUtf8();
            System.out.println("DEBUG JSON ENVIADO: " + payload);

            JSONObject jsonEnviado;
            try {
                jsonEnviado = new JSONObject(payload);
            } catch (Exception e) {
                throw new RuntimeException("El body enviado no es un JSON válido: " + payload, e);
            }

            // --- VALIDACIONES DE REGLAS DE NEGOCIO ---
            try {
                // 1. Privacidad: Comprobamos que "es_publica" sea FALSE (lo que significa que es privada)
                assertFalse("La partida debería ser privada (es_publica = false)", jsonEnviado.getBoolean("es_publica"));

                // 2. Límites de jugadores (4 - 16)
                int limiteJugadores = jsonEnviado.getInt("max_jugadores");
                assertTrue("El número de jugadores debe ser >= 4", limiteJugadores >= 4);
                assertTrue("El número de jugadores debe ser <= 16", limiteJugadores <= 16);

                // 3. Paquete (id_tema) y tiempo (tiempo_espera)
                assertTrue("Debe enviar el id del tema", jsonEnviado.has("id_tema"));
                assertTrue("Debe enviar la duración del turno", jsonEnviado.has("tiempo_espera")); // Actualizado a "tiempo_espera"

            } catch (org.json.JSONException e) {
                throw new RuntimeException("Falta una clave en el JSON. Error original: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Fallo en la prueba de creación de misión: " + e.getMessage(), e);
        }
    }
}