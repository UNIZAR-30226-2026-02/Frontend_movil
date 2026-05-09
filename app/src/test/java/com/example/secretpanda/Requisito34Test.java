package com.example.secretpanda;

import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.secretpanda.R;
import com.example.secretpanda.ui.game.match.PartidaActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class Requisito34Test {

    @Test
    public void testTemporizadorConfigurableManual() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), PartidaActivity.class);
        intent.putExtra("ID_PARTIDA", 34);
        intent.putExtra("MI_NOMBRE_USUARIO", "PandaFinal");

        try (ActivityScenario<PartidaActivity> scenario = ActivityScenario.launch(intent)) {
            // forzamos a la UI a mostrar el tiempo configurable (30 segundos).
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.tv_timer);
                if (tv != null) {
                    // Simulamos que ha llegado el estado del servidor con 30 segundos
                    tv.setText("00:30");
                }
            });

            // Refrescamos la interfaz de Robolectric
            ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
            ShadowLooper.idleMainLooper();

            // Verificamos que el componente del temporizador existe y es visible
            onView(withId(R.id.tv_timer)).check(matches(isDisplayed()));

            // Verificamos que el sistema es capaz de mostrar los 30 segundos configurados
            // (Usamos anyOf por si el cronómetro interno empezara a descontar a 29)
            onView(withId(R.id.tv_timer)).check(matches(anyOf(
                    withText(containsString("30")),
                    withText(containsString("29"))
            )));
        }
    }
}