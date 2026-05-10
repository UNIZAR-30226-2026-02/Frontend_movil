package com.example.secretpanda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.game.join.MisionPrivadaActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class Requisito14_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);

        // Obtenemos la URL exacta del mock y le quitamos la barra final para evitar errores "http://localhost:8080//partida"
        String mockUrl = mockWebServer.url("").toString();
        NetworkConfig.BASE_URL = mockUrl.endsWith("/") ? mockUrl.substring(0, mockUrl.length() - 1) : mockUrl;
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testUnirsePartidaPrivada_EnviaCodigoAlServidorSinValidarTema() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id_partida\": 42}"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MisionPrivadaActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "AgenteInfiltrado");

        try (ActivityScenario<MisionPrivadaActivity> scenario = ActivityScenario.launch(intent)) {

            // Usamos 6 caracteres por si tienes una validación if(codigo.length() < 6)
            String codigoPrueba = "ABCDEF";

            scenario.onActivity(activity -> {
                try {
                    Context ctx = activity.getApplicationContext();

                    // ¡AQUÍ ESTÁ LA MAGIA! Usamos los valores reales de tu TokenManager.java
                    ctx.getSharedPreferences("SecretPandaPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("jwt_token", "token_valido")
                            .commit();

                    EditText et = activity.findViewById(R.id.et_codigo_sala);
                    et.setText(codigoPrueba);

                    // Click
                    activity.findViewById(R.id.btn_confirmar_union).performClick();
                } catch (Exception e) {
                    throw new RuntimeException("Error inyectando los datos de prueba", e);
                }
            });

            // Esperamos a que OkHttp procese la llamada
            for (int i = 0; i < 20; i++) {
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                Thread.sleep(100);
            }

            RecordedRequest peticionUnirse = mockWebServer.takeRequest(3, TimeUnit.SECONDS);

            assertNotNull("El test es correcto, pero tu MisionPrivadaActivity abortó la petición antes de enviarla. " +
                    "Revisa el código dentro de tu OnClickListener.", peticionUnirse);

            assertEquals("POST", peticionUnirse.getMethod());

            String url = peticionUnirse.getPath();
            String body = peticionUnirse.getBody().readUtf8();

            assertTrue("El código no viajó a la API.", url.contains(codigoPrueba) || body.contains(codigoPrueba));

            assertEquals("Se hicieron más llamadas de la cuenta. El requisito exige no validar temas antes de unirse.",
                    1, mockWebServer.getRequestCount());
        }
    }
}