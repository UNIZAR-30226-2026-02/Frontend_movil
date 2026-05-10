package com.example.secretpanda;

import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.PerfilActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {33},qualifiers = "w1080dp-h2280dp")
public class Requisito6_Test {

    private MockWebServer mockWebServer;
    private TokenManager tokenManager;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);

        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        tokenManager = new TokenManager(context);
        tokenManager.saveToken("test_token");
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        tokenManager.clearToken();
        Intents.release();
    }

    @Test(timeout = 30000) // Timeout de seguridad para todo el test
    public void alModificarPerfil_EnviaDatosCorrectosAlServidor() throws Exception {
        // 1. VITAL: Conectar tu App al servidor de prueba
        // Si BASE_URL es estático, debemos asegurarnos de que apunta al mock
        NetworkConfig.BASE_URL = mockWebServer.url("").toString().replaceAll("/$", "");

        // 2. Encolar respuestas comodín (enviamos varias para cubrir todos los GETs)
        String jsonPerfil = "{\"tag\":\"PandaOriginal\",\"foto_perfil\":\"panda_mago\",\"balas\":10,\"victorias\":5,\"derrotas\":2,\"num_aciertos\":1,\"num_fallos\":1}";
        for (int i = 0; i < 5; i++) {
            mockWebServer.enqueue(new MockResponse().setBody(jsonPerfil).setResponseCode(200));
        }

        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(PerfilActivity.class)) {

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // 3. Forzar los clics directamente en la instancia de la Actividad (Adiós Espresso Flaky)
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btn_editar_perfil).performClick();
            });

            // 4. Capturar el Diálogo nativo directamente de la memoria
            AlertDialog dialogoEditar = ShadowAlertDialog.getLatestAlertDialog();
            assertNotNull("El diálogo de edición no se abrió", dialogoEditar);

            // 5. Inyectar datos y simular pulsación del botón Guardar
            EditText inputNombre = dialogoEditar.findViewById(R.id.input_editar_nombre);
            Button btnGuardar = dialogoEditar.findViewById(R.id.btn_guardar_cambios);

            inputNombre.setText("AgenteSecreto007");

            // Al pulsar aquí, sabemos que ACTUALIZAR PERFIL SERVIDOR se llama sí o sí
            btnGuardar.performClick();
        }

        // 6. Sincronización para OkHttp
        // OkHttp usa un ThreadPool en segundo plano. Esto le da tiempo para lanzar el PUT.
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Thread.sleep(500);

        // 7. BÚSQUEDA INTELIGENTE DE LA PETICIÓN
        // En lugar de adivinar el orden, analizamos todas las peticiones que llegaron al servidor
        RecordedRequest peticionPut = null;
        for (int i = 0; i < 5; i++) {
            RecordedRequest r = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
            if (r == null) break; // Ya no hay más peticiones en cola

            if ("PUT".equals(r.getMethod())) {
                peticionPut = r;
                break; // ¡Encontramos nuestra petición!
            }
        }

        // 8. Verificaciones finales
        assertNotNull("El servidor nunca recibió la petición PUT", peticionPut);

        String bodyEnviado = peticionPut.getBody().readUtf8();
        assertTrue("El nombre no se envió correctamente al servidor. Body real: " + bodyEnviado,
                bodyEnviado.contains("AgenteSecreto007"));
    }

    // Método auxiliar (Asegúrate de incluirlo en tu clase de test)
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "clic forzado para saltar restricciones de visibilidad en Robolectric";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }
}