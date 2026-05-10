package com.example.secretpanda;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.profile.PerfilActivity; // Tu actividad de perfil/ajustes
import com.example.secretpanda.ui.auth.LoginActivity; // Tu actividad de login (RF-2)

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Button;

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

@RunWith(AndroidJUnit4.class)
@Config(sdk = 33, qualifiers = "w1080dp-h2280dp")
public class Requisito3_Test {

    private TokenManager tokenManager;
    private Context context;

    @Before
    public void setUp() {
        Intents.init();
        context = ApplicationProvider.getApplicationContext();
        tokenManager = new TokenManager(context);

        // GIVEN: El usuario ya tiene una sesión iniciada (simulamos un token guardado)
        tokenManager.saveToken("token_valido_para_borrar");
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void alCerrarSesion_seEliminaTokenYRedirigeALogin() {
        // 1. Verificamos que empezamos "logueados"
        assertTrue("El token debería existir al inicio", tokenManager.getToken() != null);

        // 2. Lanzamos la Activity donde está el botón de Logout (ej: Perfil o Menú)
        try (ActivityScenario<PerfilActivity> scenario = ActivityScenario.launch(PerfilActivity.class)) {

            // WHEN: Le decimos a Robolectric que ejecute el clic directamente en la vista
            scenario.onActivity(activity -> {
                Button btnCerrarSesion = activity.findViewById(R.id.btn_cerrar_sesion);
                btnCerrarSesion.performClick();
            });

            // Esperamos hasta 3 segundos a que el callback de Google termine y borre el token
            long tiempoInicio = System.currentTimeMillis();
            while (tokenManager.getToken() != null && (System.currentTimeMillis() - tiempoInicio) < 3000) {
                Thread.sleep(100); // Esperamos 0.1 segundos
                ShadowLooper.runUiThreadTasksIncludingDelayedTasks(); // Forzamos a procesar los callbacks pendientes de la UI
            }
            // ------------------------------

            // THEN:
            // 1. Verificamos que el TokenManager finalmente ha borrado el token
            assertNull("El token debería ser nulo tras cerrar sesión", tokenManager.getToken());

            // 2. Verificamos que se ha navegado de vuelta a Login
            intended(hasComponent(LoginActivity.class.getName()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
