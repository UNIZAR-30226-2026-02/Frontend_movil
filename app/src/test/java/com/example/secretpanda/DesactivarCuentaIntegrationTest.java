package com.example.secretpanda;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.auth.LoginActivity;
import com.example.secretpanda.ui.home.profile.PerfilActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 33, qualifiers = "w1080dp-h2280dp")
public class DesactivarCuentaIntegrationTest {

    private MockWebServer mockWebServer;
    private TokenManager tokenManager;

    @Before
    public void setUp() throws IOException {
        Intents.init();
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replace("/$", "");

        // Preparamos el entorno con un token válido para simular sesión iniciada
        Context context = ApplicationProvider.getApplicationContext();
        tokenManager = new TokenManager(context);
        tokenManager.saveToken("token_jwt_valido_antes_de_desactivar");
    }

    @After
    public void tearDown() throws IOException {
        Intents.release();
        mockWebServer.shutdown();
        tokenManager.clearToken();
    }

    @Test
    public void alDesactivarCuenta_LlamaAPI_BorraToken_NavegaLogin() throws Exception {
        // GIVEN: Encolamos suficientes respuestas para cubrir todas las peticiones del ciclo de vida
        // 1. GET a /jugadores (onCreate)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"tag\":\"PandaNinja\"}"));
        // 2. GET a /amigos (onCreate)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));
        // 3. GET a /jugadores (onResume - se dispara automáticamente al iniciar la Activity)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"tag\":\"PandaNinja\"}"));
        // 4. PUT a /auth/desactivar (La petición real que hace el botón de desactivar)
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        // 5. Una respuesta de seguridad por si las moscas
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(PerfilActivity.class)) {
            try {
                // WHEN: 1. Hacemos clic en el botón de Desactivar Cuenta
                scenario.onActivity(activity -> {
                    Button btnDesactivar = activity.findViewById(R.id.btn_desactivar_cuenta);
                    btnDesactivar.performClick();
                });

                ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

                // 2. Capturamos el diálogo
                android.app.AlertDialog dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
                assertNotNull("El diálogo de confirmación no llegó a mostrarse", dialog);

                Button btnConfirmar = dialog.findViewById(R.id.btn_confirmar_dialogo);

                // 3. Forzamos su habilitación y hacemos clic
                btnConfirmar.setEnabled(true);
                btnConfirmar.performClick();

                // THEN:
                // A. Buscamos la petición PUT
                RecordedRequest requestPut = null;
                for (int i = 0; i < 5; i++) {
                    RecordedRequest tempReq = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
                    if (tempReq != null && "PUT".equals(tempReq.getMethod())) {
                        requestPut = tempReq;
                        break;
                    }
                }

                assertNotNull("El servidor nunca recibió la petición de desactivación (PUT)", requestPut);
                assertTrue("La URL de desactivación no es correcta", requestPut.getPath().contains("/auth/desactivar"));

                // B. Esperamos a que OkHttp y Google terminen de procesar la respuesta
                long tiempoInicio = System.currentTimeMillis();
                while (tokenManager.getToken() != null && (System.currentTimeMillis() - tiempoInicio) < 3000) {
                    Thread.sleep(100);
                    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                }

                // C. Verificamos que el token local se ha destruido
                assertNull("El token debería ser nulo tras desactivar la cuenta", tokenManager.getToken());

                // D. Verificamos que nos manda al Login de nuevo
                intended(hasComponent(LoginActivity.class.getName()));

            } finally {
                // E. LIMPIEZA GARANTIZADA:
                // El bloque finally asegura que el CountDownTimer se mate siempre,
                // evitando el error fantasma de "UnExecutedRunnablesException".
                ShadowLooper.idleMainLooper(11, TimeUnit.SECONDS);
            }
        }
    }
}
