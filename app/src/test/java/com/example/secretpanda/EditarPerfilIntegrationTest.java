package com.example.secretpanda;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.PerfilActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {33},qualifiers = "w1080dp-h2280dp")
public class EditarPerfilIntegrationTest {

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
        // 1. Preparamos las respuestas
        String jsonPerfil = "{\"tag\":\"PandaOriginal\",\"foto_perfil\":\"panda_mago\",\"balas\":10,\"victorias\":5,\"derrotas\":2,\"num_aciertos\":1,\"num_fallos\":1}";

        // Encolamos varias por si los ciclos de vida disparan extras
        mockWebServer.enqueue(new MockResponse().setBody(jsonPerfil));
        mockWebServer.enqueue(new MockResponse().setBody("[]"));
        mockWebServer.enqueue(new MockResponse().setBody(jsonPerfil));
        mockWebServer.enqueue(new MockResponse().setBody("{\"status\":\"ok\"}"));

        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(PerfilActivity.class)) {

            // A. Navegación con forceClick (evita errores de visibilidad)
            onView(withId(R.id.tab_datos)).perform(forceClick());

            // B. Abrir diálogo
            onView(withId(R.id.btn_editar_perfil)).perform(forceClick());
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // C. Editar nombre
            onView(withId(R.id.input_editar_nombre))
                    .inRoot(isDialog())
                    .perform(replaceText("NuevoAgente"), closeSoftKeyboard());

            // D. Guardar cambios
            onView(withId(R.id.btn_guardar_cambios))
                    .inRoot(isDialog())
                    .perform(forceClick());

            // 2. VERIFICACIÓN NO BLOQUEANTE
            RecordedRequest currentRequest;
            boolean putEncontrado = false;

            // Intentamos leer hasta 5 peticiones que hayan llegado
            for (int i = 0; i < 5; i++) {
                currentRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
                if (currentRequest == null) break;

                if ("PUT".equals(currentRequest.getMethod())) {
                    putEncontrado = true;
                    String body = currentRequest.getBody().readUtf8();
                    assertTrue("El body no contiene el nuevo nombre", body.contains("NuevoAgente"));
                    break;
                }
            }

            assertTrue("El test terminó pero nunca se detectó la petición PUT", putEncontrado);
        }
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