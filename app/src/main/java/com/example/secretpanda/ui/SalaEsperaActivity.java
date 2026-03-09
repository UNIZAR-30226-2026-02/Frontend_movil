package com.example.secretpanda.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SalaEsperaActivity extends AppCompatActivity {

    private RecyclerView rvJugadores;
    private JugadorSalaAdapter adapter;

    // Variables para la lógica
    private boolean estoyEnEquipoAzul; // true = Azul, false = Rojo
    private TextView btnUnirseAzul;
    private TextView btnUnirseRojo;

    // Tu jugador
    private Jugador jugadorLocal;
    private int posicionJugadorLocal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        btnUnirseAzul = findViewById(R.id.btn_unirse_azul);
        btnUnirseRojo = findViewById(R.id.btn_unirse_rojo);

        // ==========================================
        // ASIGNACIÓN ALEATORIA AL ENTRAR (true o false)
        // ==========================================
        estoyEnEquipoAzul = new Random().nextBoolean();

        if (estoyEnEquipoAzul) {
            setModoDentro(btnUnirseAzul, "#0000FF");
            setModoUnirse(btnUnirseRojo, "#FF0000");
        } else {
            setModoDentro(btnUnirseRojo, "#FF0000");
            setModoUnirse(btnUnirseAzul, "#0000FF");
        }

        // Lógica al pulsar los botones
        btnUnirseAzul.setOnClickListener(v -> gestionarClicEquipo(true));
        btnUnirseRojo.setOnClickListener(v -> gestionarClicEquipo(false));

        // ==========================================
        // CONFIGURACIÓN DE LA LISTA
        // ==========================================
        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));

        List<Jugador> jugadoresPrueba = new ArrayList<>();

        // 1. CREAMOS TU JUGADOR con el equipo aleatorio
        jugadorLocal = new Jugador("Tú (MiUsuario)");
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        jugadoresPrueba.add(jugadorLocal);
        posicionJugadorLocal = 0;

        // 2. Añadimos otros jugadores de prueba
        Jugador j2 = new Jugador("GabriThePro");
        j2.setEsEquipoAzul(false); // Rojo
        jugadoresPrueba.add(j2);

        Jugador j3 = new Jugador("PandaNinja");
        j3.setEsEquipoAzul(true); // Azul
        jugadoresPrueba.add(j3);

        adapter = new JugadorSalaAdapter(jugadoresPrueba);
        rvJugadores.setAdapter(adapter);
        // Botón de la estrella (Temáticas)
        findViewById(R.id.btn_tematicas).setOnClickListener(v -> mostrarDialogoTematicas());

        // Botón de la tuerca (Configuración)
        findViewById(R.id.btn_configuracion).setOnClickListener(v -> {
            // Aquí puedes abrir otra configuración si quieres
        });
    }

    // ==========================================
    // MÉTODOS PARA CONTROLAR LOS BOTONES Y LA LISTA
    // ==========================================

    private void gestionarClicEquipo(boolean pulsadoAzul) {
        // Si pulsas el botón del equipo en el que ya estás, no hacemos nada
        if (estoyEnEquipoAzul == pulsadoAzul) {
            return;
        }

        // Si pulsas el equipo contrario, te cambiamos
        estoyEnEquipoAzul = pulsadoAzul;

        if (estoyEnEquipoAzul) {
            // Me paso al AZUL
            setModoDentro(btnUnirseAzul, "#0000FF");
            setModoUnirse(btnUnirseRojo, "#FF0000");
        } else {
            // Me paso al ROJO
            setModoDentro(btnUnirseRojo, "#FF0000");
            setModoUnirse(btnUnirseAzul, "#0000FF");
        }

        // ¡MAGIA! Actualizamos tu tarjeta al nuevo equipo
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        adapter.notifyItemChanged(posicionJugadorLocal);
    }

    // Pinta el botón con el borde de color y fondo transparente
    private void setModoDentro(TextView btn, String colorHex) {
        btn.setText("Dentro");
        btn.setBackgroundResource(R.drawable.bg_boton_dentro);

        GradientDrawable fondo = (GradientDrawable) btn.getBackground().mutate();
        fondo.setStroke(5, Color.parseColor(colorHex));

        btn.setTextColor(Color.parseColor(colorHex));
        btn.setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFFFFF"));
    }

    // Pinta el botón con el fondo sólido de color
    private void setModoUnirse(TextView btn, String colorHex) {
        btn.setText("Unirse");
        btn.setBackgroundResource(R.drawable.bg_boton_unirse);

        GradientDrawable fondo = (GradientDrawable) btn.getBackground().mutate();
        fondo.setColor(Color.parseColor(colorHex));
        fondo.setStroke(0, Color.TRANSPARENT);

        btn.setTextColor(Color.parseColor("#FFFFFF"));
        btn.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT);
    }
    // ==========================================
    // DIÁLOGO DE ABANDONAR PARTIDA
    // ==========================================
    private void mostrarDialogoAbandonar() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_abandonar);

        // Hacemos el fondo transparente para que se vea tu diseño con márgenes
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

            // Opcional: Hacemos que ocupe el ancho de la pantalla para que respete tus márgenes de 24dp
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Si el usuario pulsa la "X", simplemente cerramos el diálogo y se queda en la sala
        dialog.findViewById(R.id.btn_cerrar_abandonar).setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Si el usuario confirma en el botón rojo, cerramos el diálogo y abandonamos la sala
        dialog.findViewById(R.id.btn_confirmar_abandonar).setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Esto cierra la SalaEsperaActivity y te devuelve a la pantalla anterior
        });

        dialog.show();
    }

    private void mostrarDialogoTematicas() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_tematicas_sala);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            // Hacemos que aparezca desde abajo o centrado
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Configurar el botón de cerrar del diálogo
        dialog.findViewById(R.id.btn_cerrar_tematicas).setOnClickListener(v -> dialog.dismiss());

        // Aquí luego podrás programar que al tocar una carta (Flores, Lápices...)
        // se cambie la temática de la partida.

        dialog.show();
    }
}
