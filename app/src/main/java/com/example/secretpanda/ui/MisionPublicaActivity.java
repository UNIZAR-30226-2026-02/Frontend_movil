package com.example.secretpanda.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Partida;

import java.util.ArrayList;
import java.util.List;

public class MisionPublicaActivity extends AppCompatActivity implements TematicasDialogFragment.TematicaListener {

    private RecyclerView rvPartidas;
    private PartidaAdapter adapter;
    private View btnSelectorTematica; // Cambiado de Spinner a View (el layout del filtro)
    private TextView tvTematicaSeleccionada;
    private FrameLayout btnHome;

    // Listas para el filtrado
    private List<Partida> todasLasPartidas;
    private List<Partida> partidasFiltradas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_misiones_publicas);

        // 1. Inicializar vistas
        btnHome = findViewById(R.id.btn_volver_home);
        rvPartidas = findViewById(R.id.rv_misiones);
        btnSelectorTematica = findViewById(R.id.btn_selector_tematicas); // El ID de tu layout de filtro
        tvTematicaSeleccionada = findViewById(R.id.tv_tematica_actual); // Asegúrate de tener este ID en el XML

        btnHome.setOnClickListener(v -> finish());

        // AL PULSAR EL BOTÓN, ABRIMOS TU DIALOGFRAGMENT
        btnSelectorTematica.setOnClickListener(v -> {
            TematicasDialogFragment dialog = new TematicasDialogFragment();
            dialog.setTematicaListener(this); // Escuchamos la respuesta
            dialog.show(getSupportFragmentManager(), "DialogoTematicas");
        });

        // 3. Cargar datos iniciales
        prepararDatos();
    }

    private void prepararDatos() {
        todasLasPartidas = new ArrayList<>();
        // Hardcodeamos las partidas con su respectiva temática
        todasLasPartidas.add(new Partida("Animals", "GabriThePro", "120s", "10/10", false, "Animales"));
        todasLasPartidas.add(new Partida("Perros", "GabriThePro", "120s", "10/10", false, "Perros"));
        todasLasPartidas.add(new Partida("Gatos", "GabriThePro", "120s", "10/10", true, "Gatos"));
        todasLasPartidas.add(new Partida("Coches", "Admin", "60s", "5/10", false, "Coches"));

        // Al inicio, mostramos todas
        partidasFiltradas = new ArrayList<>(todasLasPartidas);

        adapter = new PartidaAdapter(partidasFiltradas);
        rvPartidas.setLayoutManager(new LinearLayoutManager(this));
        rvPartidas.setAdapter(adapter);
    }

    // MÉTODO DE FILTRADO (Viene de la interfaz del Dialog)
    @Override
    public void onTematicaSelected(String tematica) {
        // Actualizar el texto del botón selector en la UI
        if (tvTematicaSeleccionada != null) {
            tvTematicaSeleccionada.setText(tematica);
        }

        // Limpiar la lista actual
        partidasFiltradas.clear();

        if (tematica.equals("Todas las temáticas")) {
            partidasFiltradas.addAll(todasLasPartidas);
        } else {
            // Filtrar por nombre de temática
            for (Partida p : todasLasPartidas) {
                if (p.getTematica().equalsIgnoreCase(tematica)) {
                    partidasFiltradas.add(p);
                }
            }
        }

        // Notificar al adaptador que los datos han cambiado para refrescar la lista
        adapter.notifyDataSetChanged();
    }
}
