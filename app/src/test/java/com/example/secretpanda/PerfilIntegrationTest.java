package com.example.secretpanda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.PerfilActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import com.example.secretpanda.data.NetworkConfig;

import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {33}, qualifiers = "w1080dp-h2280dp")
public class PerfilIntegrationTest {

    private MockWebServer mockWebServer;
    private TokenManager tokenManager;

    @Before
    public void setUp() throws Exception {
        // 1. Iniciamos el servidor falso (Robolectric y MockWebServer asignarán un puerto libre)
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 2. Redirigimos las peticiones de la app a nuestro servidor falso
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replace("/$", "");

        // 3. Inicializamos Intents de Espresso
        init();

        // 4. Preparamos un token falso para que la Activity no nos eche al Login
        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        tokenManager = new TokenManager(context);
        tokenManager.saveToken("token_de_prueba_jwt");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        release();
        tokenManager.clearToken();
    }

    @Test
    public void alAbrirPerfil_MuestraInformacionPersonalYEstadisticas() throws Exception {
        // GIVEN: JSON de respuesta que simula los datos exactos del backend
        String jsonPerfilMock = "{" +
                "\"tag\":\"PandaSupremo\"," +
                "\"foto_perfil\":\"panda_mago\"," +
                "\"balas\":42," +
                "\"victorias\":10," +
                "\"derrotas\":5," +
                "\"num_aciertos\":8," +
                "\"num_fallos\":2" +
                "}";

        // Encolamos las respuestas en el orden en que PerfilActivity hace las peticiones
        // 1. GET /jugadores (Disparado en onCreate -> cargarDatosPerfil)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonPerfilMock));
        // 2. GET /amigos (Disparado en onCreate -> cargarAmigosServidor)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));
        // 3. GET /jugadores (Disparado en onResume -> cargarDatosPerfil)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonPerfilMock));

        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(PerfilActivity.class)) {

            // --- DIAGNÓSTICO DE RED ---
            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertNotNull("El servidor Mock no recibió la petición GET a /jugadores", request);

            // --- ESPERA ACTIVA MEJORADA ---
            long startTime = System.currentTimeMillis();
            // ¡Corrección del array! Usamos un array de 1 posición para poder modificarlo desde la lambda
            final String[] nombreObtenido = new String[1];
            boolean cargado = false;

            while ((System.currentTimeMillis() - startTime) < 5000) {
                // Forzamos a Robolectric a procesar el hilo principal y asíncronos (OkHttp)
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

                scenario.onActivity(activity -> {
                    TextView txtNombre = activity.findViewById(R.id.texto_nombre_datos);
                    nombreObtenido[0] = txtNombre.getText().toString();
                });

                // "Espía Secreto" es el valor por defecto en tu PerfilActivity
                if (nombreObtenido[0] != null && !"Espía Secreto".equals(nombreObtenido[0])) {
                    cargado = true;
                    break;
                }
                Thread.sleep(100);
            }

            assertTrue("Timeout: La UI no se actualizó con los datos del servidor. Texto actual: " + nombreObtenido[0], cargado);

            // THEN: Validamos que los datos se han renderizado en la interfaz
            scenario.onActivity(activity -> {
                assertEquals("PandaSupremo", ((TextView)activity.findViewById(R.id.texto_nombre_datos)).getText().toString());
                assertEquals("42", ((TextView)activity.findViewById(R.id.texto_mis_balas)).getText().toString());

                // La app suma victorias (10) + derrotas (5) para calcular el total
                assertEquals("15", ((TextView)activity.findViewById(R.id.stat_mio_partidas)).getText().toString());
            });
        }
    }
}
