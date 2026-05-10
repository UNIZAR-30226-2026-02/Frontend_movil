package com.example.secretpanda;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.game.match.PartidaActivity;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
// 🔥 Aplicamos los dos escudos: STOMP y OkHttp
@Config(sdk = 34, shadows = {ShadowStompClient.class, ShadowOkHttpClient.class})
public class Requisito23_5Test {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test(timeout = 15000)
    public void testIniciarPartidaConMinimosSinLlegarAlMaximo() throws Exception {

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SalaEsperaActivity.class);
        intent.putExtra("ID_PARTIDA", 101);
        intent.putExtra("MI_NOMBRE_USUARIO", "Lider");
        intent.putExtra("ES_LIDER", true);

        try (ActivityScenario<SalaEsperaActivity> scenario = ActivityScenario.launch(intent)) {
            ShadowLooper.idleMainLooper();

            scenario.onActivity(activity -> {
                try {
                    // Creamos 4 jugadores (2 Azules, 2 Rojos)
                    List<Jugador> fakeJugadores = new ArrayList<>();
                    Jugador j1 = new Jugador("Lider"); j1.setEsEquipoAzul(true);
                    Jugador j2 = new Jugador("A2"); j2.setEsEquipoAzul(true);
                    Jugador j3 = new Jugador("R1"); j3.setEsEquipoAzul(false);
                    Jugador j4 = new Jugador("R2"); j4.setEsEquipoAzul(false);
                    fakeJugadores.add(j1); fakeJugadores.add(j2); fakeJugadores.add(j3); fakeJugadores.add(j4);

                    // Forzamos la variable privada maxJugadores a 8
                    Field maxJugadoresField = SalaEsperaActivity.class.getDeclaredField("maxJugadores");
                    maxJugadoresField.setAccessible(true);
                    maxJugadoresField.set(activity, 8);

                    // Forzamos la variable privada listaJugadores
                    Field listaField = SalaEsperaActivity.class.getDeclaredField("listaJugadores");
                    listaField.setAccessible(true);
                    listaField.set(activity, fakeJugadores);

                    // Hacemos visible el botón
                    View btn = activity.findViewById(R.id.btn_iniciar_partida_principal);
                    btn.setVisibility(View.VISIBLE);
                    btn.setEnabled(true);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            ShadowLooper.idleMainLooper();

            // pulsamos el botón principal
            onView(withId(R.id.btn_iniciar_partida_principal)).perform(click());
            ShadowLooper.idleMainLooper();

            // debería salir el diálogo de confirmación porque 4 < 8
            onView(withId(R.id.btn_iniciar_confirmado))
                    .inRoot(isDialog())
                    .perform(click());

            ShadowLooper.idleMainLooper();
        }
    }
}