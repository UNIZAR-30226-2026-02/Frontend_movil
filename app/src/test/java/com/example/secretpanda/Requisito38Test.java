package com.example.secretpanda;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.ui.audio.MusicaService;
import com.example.secretpanda.ui.home.HomeActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowSeekBar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34, shadows = {Requisito38Test.ShadowMusicaService.class})
public class Requisito38Test {

    // SOMBRA PARA INTERCEPTAR EL VOLUMEN
    @Implements(MusicaService.class)
    public static class ShadowMusicaService {
        public static float volumenRecibido = -1f;
        @Implementation
        public static void setVolumen(float volumen) {
            volumenRecibido = volumen;
        }
    }

    @Before
    public void prepararNuevaSesion() {
        ApplicationProvider.getApplicationContext()
                .getSharedPreferences("Ajustes_Audio", Context.MODE_PRIVATE)
                .edit().clear().apply();

        ShadowMusicaService.volumenRecibido = -1f;
    }

    @Test
    public void testAjustesIndependientesYReseteo() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaDJ");

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {

            // Navegación al diálogo
            onView(withId(R.id.btn_menu_opciones)).perform(click());
            ShadowLooper.idleMainLooper();

            onView(withId(R.id.btn_opcion_musica))
                    .inRoot(isPlatformPopup())
                    .perform(click());
            ShadowLooper.idleMainLooper();

            // Están en 50 y 80 al empezar la sesión
            onView(withId(R.id.seekbar_musica_fondo))
                    .inRoot(isDialog())
                    .check(matches(conProgreso(50)));

            onView(withId(R.id.seekbar_efectos_sonido))
                    .inRoot(isDialog())
                    .check(matches(conProgreso(80)));

            // Cambiamos música al 100%
            onView(withId(R.id.seekbar_musica_fondo))
                    .inRoot(isDialog())
                    .perform(forzarCambioUsuario(100));

            ShadowLooper.idleMainLooper();

            // El servicio ha recibido el volumen 1.0 (100/100)
            assertEquals("El volumen de música debería ser 1.0f", 1.0f, ShadowMusicaService.volumenRecibido, 0.01f);

            // Verificamos que efectos sigue en 80 (Ajuste independiente)
            onView(withId(R.id.seekbar_efectos_sonido))
                    .inRoot(isDialog())
                    .check(matches(conProgreso(80)));
        }
    }


    public static Matcher<View> conProgreso(final int progreso) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override public void describeTo(Description d) { d.appendText("con progreso: " + progreso); }
            @Override protected boolean matchesSafely(SeekBar sb) { return sb.getProgress() == progreso; }
        };
    }

    public static ViewAction forzarCambioUsuario(final int progreso) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isAssignableFrom(SeekBar.class); }
            @Override public String getDescription() { return "cambiar progreso como usuario real"; }
            @Override public void perform(UiController ui, View view) {
                SeekBar sb = (SeekBar) view;
                sb.setProgress(progreso);

                // Usamos el Shadow de Robolectric para disparar el listener
                // enviando 'fromUser = true'.
                ShadowSeekBar shadow = Shadows.shadowOf(sb);
                if (shadow.getOnSeekBarChangeListener() != null) {
                    shadow.getOnSeekBarChangeListener().onProgressChanged(sb, progreso, true);
                }
            }
        };
    }
}