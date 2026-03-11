package com.example.secretpanda.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;

import java.util.ArrayList;
import java.util.List;

public class ConfiguracionMisionActivity extends AppCompatActivity {

    private boolean esPrivada;

    // Variables para guardar lo que ha seleccionado el usuario
    private String tematicaSeleccionada = "";
    private int tiempoSeleccionado = 60; // Por defecto
    private int jugadoresSeleccionados = 8; // Por defecto

    private TextView txtTematica;
    private TextView[] botonesTiempo;
    private TextView[] botonesJugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_configuracion_mision);

        esPrivada = getIntent().getBooleanExtra("ES_PRIVADA", false);

        // Si es privada, le ponemos el texto al título
        TextView txtTitulo = findViewById(R.id.txt_titulo_config);
        if (esPrivada) txtTitulo.setText("Configurar misión privada");
        else txtTitulo.setText("Configurar misión pública");

        FrameLayout btnVolver = findViewById(R.id.btn_volver_home_config);
        btnVolver.setOnClickListener(v -> finish());

        LinearLayout btnTematica = findViewById(R.id.btn_desplegable_tematica_crear);
        txtTematica = findViewById(R.id.txt_tematica_elegida_crear);
        btnTematica.setOnClickListener(v -> mostrarDialogoTematicas());

        botonesTiempo = new TextView[]{
                findViewById(R.id.btn_tiempo_30),
                findViewById(R.id.btn_tiempo_60),
                findViewById(R.id.btn_tiempo_90),
                findViewById(R.id.btn_tiempo_120)
        };
        configurarBotonesSeleccion(botonesTiempo, new int[]{30, 60, 90, 120}, true);
        seleccionarBoton(botonesTiempo[1]); // Selecciona 60s por defecto

        botonesJugadores = new TextView[]{
                findViewById(R.id.btn_jug_4),
                findViewById(R.id.btn_jug_6),
                findViewById(R.id.btn_jug_8),
                findViewById(R.id.btn_jug_10)
        };
        configurarBotonesSeleccion(botonesJugadores, new int[]{4, 6, 8, 10}, false);
        seleccionarBoton(botonesJugadores[2]); // Selecciona 8 por defecto

        TextView btnCrear = findViewById(R.id.btn_crear_mision_final);
        btnCrear.setOnClickListener(v -> {
            if (tematicaSeleccionada.isEmpty() || tematicaSeleccionada.equals("Selecciona temática...")) {
                Toast.makeText(this, "Debes seleccionar una temática", Toast.LENGTH_SHORT).show();
                return;
            }

            String mensaje = "Creando sala... " + (esPrivada ? "PRIVADA" : "PÚBLICA") +
                    "\nTema: " + tematicaSeleccionada +
                    "\nTiempo: " + tiempoSeleccionado + "s" +
                    "\nJugadores: " + jugadoresSeleccionados;

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        });
    }

    private void mostrarDialogoTematicas() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();

        dialog.setConfiguracionFiltros(true, false);

        dialog.setTematicaListener(tematica -> {
            tematicaSeleccionada = tematica;
            txtTematica.setText(tematicaSeleccionada);
        });

        dialog.show(getSupportFragmentManager(), "TematicasDialogConfig");
    }

    // Método ayudante para que los botones actúen como "Radio Buttons" visuales
    private void configurarBotonesSeleccion(TextView[] botones, int[] valores, boolean esTiempo) {
        for (int i = 0; i < botones.length; i++) {
            final int index = i;
            botones[i].setOnClickListener(v -> {
                for (TextView btn : botones) {
                    btn.setBackgroundResource(R.drawable.fondo_btn_unirse_pequeno); // Tu fondo gris
                }
                seleccionarBoton(botones[index]);

                if (esTiempo) tiempoSeleccionado = valores[index];
                else jugadoresSeleccionados = valores[index];
            });
        }
    }

    private void seleccionarBoton(TextView boton) {
        boton.setBackgroundResource(R.drawable.borde_verde_seleccion_personalizacion);
    }
}