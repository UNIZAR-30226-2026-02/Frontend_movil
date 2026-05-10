package com.example.secretpanda;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.LoadingActivity;
import com.example.secretpanda.ui.auth.UserSelectionActivity;

// Importaciones de Espresso
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class) // Magia: Ejecutará Espresso usando Robolectric por debajo
@Config(sdk = 33, qualifiers = "w1080dp-h2280dp")
public class Requisito1_Test {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        Intents.init();
        mockWebServer = new MockWebServer();
        mockWebServer.start(); // En Robolectric no hace falta forzar el puerto 8080

        // IMPORTANTE: Redirigimos la app a la URL dinámica de nuestro servidor local falso
        // (Asegúrate de que BASE_URL en NetworkConfig no tenga la palabra 'final')
        NetworkConfig.BASE_URL = mockWebServer.url("/api").toString();
    }

    @After
    public void tearDown() throws IOException {
        Intents.release();
        mockWebServer.shutdown();
    }

    @Test
    public void alRegistrarseConExito_NavegaALoading() throws Exception {
        // GIVEN: El servidor responde con un Token JWT
        String tokenSimulado = "jwt_robo_panda_123";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"token\": \"" + tokenSimulado + "\"}"));

        // Preparamos el Intent con los datos necesarios
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, UserSelectionActivity.class);
        intent.putExtra("GOOGLE_ID_ESTABLE", "google_123");
        intent.putExtra("ID_GOOGLE", "google_123");

        // Lanzamos la Activity simulada
        try (ActivityScenario<UserSelectionActivity> scenario = ActivityScenario.launch(intent)) {

            // WHEN: Escribimos en la interfaz usando Espresso
            onView(withId(R.id.inputUsuario))
                    .perform(typeText("PandaTest"), closeSoftKeyboard());

            onView(withId(R.id.btnAceptar)).perform(click());

            // Pausa breve y forzamos a Robolectric a procesar el hilo principal
            // tras la respuesta asíncrona de OkHttp
            Thread.sleep(500);
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

            // THEN: Comprobamos resultados

            // 1. Verificamos que se hizo la petición correcta a la "base de datos"
            RecordedRequest request = mockWebServer.takeRequest();
            // Ojo: mockWebServer.url("/api") ya añade la barra al final, así que ajustamos la ruta esperada
            assertEquals("/api/auth/registro", request.getPath());

            // 2. Verificamos que el TokenManager guardó el token otorgando acceso
            TokenManager tm = new TokenManager(context);
            assertEquals(tokenSimulado, tm.getToken());

            // 3. Verificamos que la UI navegó a LoadingActivity con los datos correctos
            intended(allOf(
                    hasComponent(LoadingActivity.class.getName()),
                    hasExtra("MI_NOMBRE_USUARIO", "PandaTest"),
                    hasExtra("DESTINO", "HOME")
            ));
        }
    }
}

