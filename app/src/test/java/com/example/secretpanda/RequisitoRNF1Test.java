package com.example.secretpanda;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.game.match.PartidaActivity;
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

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
// Usamos tu Shadow del StompClient para que no pete la PartidaActivity al intentar conectar
@Config(sdk = 34, shadows = {ShadowStompClient.class})
public class RequisitoRNF1Test {

    private MockWebServer mockWebServer;
    private TokenManager tokenManager;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        tokenManager = new TokenManager(context);
        tokenManager.saveToken("token_sesion_valido"); // Simulamos sesión activa

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        NetworkConfig.BASE_URL = mockWebServer.url("/").toString();
        NetworkConfig.WS_URL = mockWebServer.url("/ws").toString().replace("http", "ws");

        // Iniciamos el capturador de Intents de Espresso
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        tokenManager.clearToken();
        mockWebServer.shutdown();
        Intents.release();
    }

    @Test
    public void testReconexionInteligente_SaltaAPartidaSiHayUnaActiva() {
        // 1. MOCK: El servidor nos dice que estamos a medias en la partida 99
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"balas\": 150, \"foto_perfil\": \"img.png\", \"partida_activa_id\": 99}"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaReconectado");

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {
            // Damos tiempo a la petición HTTP asíncrona a terminar
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // 2. VERIFICACIÓN: Comprobamos que HomeActivity ha lanzado un Intent hacia PartidaActivity
            // y que además le ha pasado el ID correcto (99) de la partida a recuperar.
            intended(allOf(
                    hasComponent(PartidaActivity.class.getName()),
                    hasExtra("ID_PARTIDA", 99)
            ));
        }
    }

    @Test
    public void testSesionUnica_ExpulsaYBorraTokenSiDaError403() {
        // 1. MOCK: El servidor devuelve un 403 (Sesión Iniciada en Web) a todas las peticiones
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(403)
                        .setBody("{\"error_code\":\"SESSION_INVALIDATED\"}");
            }
        });

        // 2. Simulamos que el usuario intenta entrar a jugar
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 45);

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            scenario.onActivity(activity -> {
                // 3. VERIFICACIÓN: El token debió borrarse (Requisito de seguridad cumplido)
                assertNull("El token debe ser nulo porque la sesión fue invalidada", tokenManager.getToken());

                // 4. VERIFICACIÓN: La actividad debe estar cerrándose para expulsar al usuario
                assertTrue("La PartidaActivity debió llamar a finish() tras el 403", activity.isFinishing());
            });
        }
    }
}