package com.example.secretpanda;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.ui.audio.EfectosManager;
import com.example.secretpanda.ui.audio.MusicaService;
import com.example.secretpanda.ui.home.HomeActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {Requisito37Test.ShadowEfectosManager.class})
public class Requisito37Test {

    // SOMBRA PARA INTERCEPTAR SONIDOS
    @Implements(EfectosManager.class)
    public static class ShadowEfectosManager {
        public static int ultimoSonidoReproducido = -1;
        public static boolean reproducirLlamado = false;

        @Implementation
        public static void reproducir(Context context, int resourceId) {
            ultimoSonidoReproducido = resourceId;
            reproducirLlamado = true;
        }
    }

    @Test
    public void testMusicaYFondoYEfectosDeSonido() {
        // Reiniciamos el estado de la sombra
        ShadowEfectosManager.reproducirLlamado = false;
        ShadowEfectosManager.ultimoSonidoReproducido = -1;

        //Iniciamos la HomeActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaAuditivo");

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {

            // Música de fondo
            // Comprobamos que la actividad envió el Intent para iniciar MusicaService
            Intent serviceIntent = ShadowApplication.getInstance().getNextStartedService();
            assertNotNull("El sistema debe iniciar el servicio de música", serviceIntent);
            assertEquals(MusicaService.class.getName(), serviceIntent.getComponent().getClassName());

            // Efectos de sonido al pulsar botones
            scenario.onActivity(activity -> {
                // Buscamos el botón de perfil (que sabemos que tiene sonido de click)
                View btnPerfil = activity.findViewById(R.id.btn_perfil);
                assertNotNull("El botón de perfil debe existir", btnPerfil);

                // Simulamos la pulsación
                btnPerfil.performClick();
            });

            // Comprobamos que el EfectosManager fue invocado
            assertTrue("Se debe invocar al EfectosManager al pulsar un botón",
                    ShadowEfectosManager.reproducirLlamado);

            // Verificamos que el sonido enviado es el correcto (sonido_click)
            assertEquals("El efecto reproducido debe ser el sonido de click",
                    R.raw.sonido_click, ShadowEfectosManager.ultimoSonidoReproducido);
        }
    }
}