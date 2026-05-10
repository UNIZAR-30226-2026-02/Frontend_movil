package com.example.secretpanda;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.auth.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 33, qualifiers = "w1080dp-h2280dp")
public class Requisito2_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        Intents.init();
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Ojo aquí: asegúrate de que esto coincide con cómo formas tu URL.
        // Tu código añade "/auth/login" directamente a BASE_URL.
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replace("/$", "");
    }

    @After
    public void tearDown() throws IOException {
        Intents.release();
        mockWebServer.shutdown();
    }

    @Test
    public void alIniciarSesionConGoogle_GuardaTokenYNavegaAHome() throws Exception {
        // GIVEN: El backend responde con un usuario existente y su token
        String tokenSimulado = "jwt_super_seguro_999";
        String googleIdEstableSimulado = "google_id_12345";

        // Creamos el JSON exactamente como lo espera tu método
        String jsonRespuesta = "{"
                + "\"es_nuevo\": false,"
                + "\"token\": \"" + tokenSimulado + "\","
                + "\"jugador\": {"
                + "    \"partida_activa_id\": 0,"
                + "    \"tag\": \"PandaNinja\""
                + "}"
                + "}";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonRespuesta));

        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {

            // WHEN: Inyectamos el éxito de Google llamando directamente a tu método privado
            scenario.onActivity(activity -> {
                try {
                    // Magia de Java (Reflexión) para invocar un método privado en el test
                    Method method = LoginActivity.class.getDeclaredMethod("enviarTokenAlBackend", String.class, String.class);
                    method.setAccessible(true);
                    // Le pasamos el idToken y el googleIdEstable
                    method.invoke(activity, "token_google_falso", googleIdEstableSimulado);
                } catch (Exception e) {
                    throw new RuntimeException("Error llamando al método privado", e);
                }
            });

            // Esperamos a que OkHttp termine su llamada asíncrona y el runOnUiThread se ejecute
            Thread.sleep(1000);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // THEN: Comprobamos resultados

            // 1. Verificamos la petición de red (con límite de tiempo para que no se congele)
            RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
            assertNotNull("El servidor nunca recibió la petición", request);

            // Opcional: Verificar que el JSON enviado contiene el id_google
            String bodyEnviado = request.getBody().readUtf8();
            org.junit.Assert.assertTrue(bodyEnviado.contains("token_google_falso"));

            // 2. Verificamos que el TokenManager guardó los datos correctamente
            Context context = ApplicationProvider.getApplicationContext();
            TokenManager tm = new TokenManager(context);
            assertEquals("El TokenManager no guardó el token JWT", tokenSimulado, tm.getToken());
            assertEquals("El TokenManager no guardó el ID de Google", googleIdEstableSimulado, tm.getIdGoogle());

            // 3. Verificamos que navegamos a la pantalla de Home
            // NOTA: Cambia HomeActivity.class por la Activity real que abre tu método irAHome()
            intended(allOf(
                    hasComponent(com.example.secretpanda.ui.LoadingActivity.class.getName()),
                    hasExtra("DESTINO", "HOME"),
                    hasExtra("MI_NOMBRE_USUARIO", "PandaNinja")
            ));
        }
    }
}