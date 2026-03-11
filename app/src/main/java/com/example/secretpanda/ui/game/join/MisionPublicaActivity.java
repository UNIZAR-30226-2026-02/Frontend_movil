package com.example.secretpanda.ui.game.join;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.Partida;
import com.example.secretpanda.ui.game.match.PartidaAdapter;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

import java.util.ArrayList;
import java.util.List;

public class MisionPublicaActivity extends AppCompatActivity {

    private RecyclerView recyclerMisiones;
    private FrameLayout btnCerrar;

    private LinearLayout btnSelectorTematicas;
    private TextView tvTematicaActual;

    private PartidaAdapter adapter;
    private List<Partida> listaPartidasTodas; // Para guardar todas
    private List<Partida> listaPartidasFiltradas; // Para guardar las que se muestran

    private String tematicaFiltroActual = "Todas las temáticas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_misiones_publicas);

        recyclerMisiones = findViewById(R.id.rv_misiones);
        btnCerrar = findViewById(R.id.btn_volver_home);

        btnSelectorTematicas = findViewById(R.id.btn_selector_tematicas);
        tvTematicaActual = findViewById(R.id.tv_tematica_actual);

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> finish());

        if (recyclerMisiones != null) {
            recyclerMisiones.setLayoutManager(new LinearLayoutManager(this));
        }

        if (btnSelectorTematicas != null) {
            btnSelectorTematicas.setOnClickListener(v -> mostrarDialogoFiltro());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMisionesAutomaticas();
    }

    // LÓGICA PARA ABRIR Y SELECCIONAR TEMÁTICAS
    private void mostrarDialogoFiltro() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();

        dialog.setTematicaListener(tematica -> {
            tematicaFiltroActual = tematica;

            if (tvTematicaActual != null) {
                tvTematicaActual.setText(tematicaFiltroActual);
            }

            aplicarFiltro();
        });

        dialog.show(getSupportFragmentManager(), "TematicasDialog");
    }

    // GENERAR PARTIDAS ALEATORIAS
    private void cargarMisionesAutomaticas() {
        listaPartidasTodas = new ArrayList<>();
        java.util.Random random = new java.util.Random();

        List<ItemPersonalizacion> todosLosItems = InventarioGlobal.getInstance().getTodosLosItems();
        List<ItemPersonalizacion> tematicasTienda = new ArrayList<>();
        for (ItemPersonalizacion item : todosLosItems) {
            if ("baraja".equals(item.getTipo())) tematicasTienda.add(item);
        }

        if (tematicasTienda.isEmpty()) return;

        for (int i = 1; i <= 15; i++) {
            ItemPersonalizacion temaAleatorio = tematicasTienda.get(random.nextInt(tematicasTienda.size()));

            // Lógica numérica real (según tu DB de 4 a 16, por ejemplo 8)
            int maxJugadores = 8;
            int numJugadoresActuales = random.nextInt(maxJugadores) + 1;

            // Forzar alguna vacía y alguna llena para probar
            if (random.nextBoolean() && numJugadoresActuales == maxJugadores)
                numJugadoresActuales = maxJugadores - 2;

            String nombrePartida = "Misión " + i;
            String creador = "Agente_" + random.nextInt(999);
            String tiempo = "120s";
            boolean estaBloqueada = temaAleatorio.isBloqueado();

            listaPartidasTodas.add(new Partida(nombrePartida, creador, tiempo, numJugadoresActuales, maxJugadores, estaBloqueada, temaAleatorio.getNombre()));
        }

        aplicarFiltro();
    }

    private void aplicarFiltro() {
        listaPartidasFiltradas = new ArrayList<>();

        for (Partida p : listaPartidasTodas) {
            if (tematicaFiltroActual.equals("Todas las temáticas") || p.getTematica().equals(tematicaFiltroActual)) {
                listaPartidasFiltradas.add(p);
            }
        }

        adapter = new PartidaAdapter(listaPartidasFiltradas, partida -> {

            if (partida.isBloqueada() || partida.isLlena()) {

                Dialog dialogError = new Dialog(MisionPublicaActivity.this);
                dialogError.setContentView(R.layout.dialog_error_mision);

                if (dialogError.getWindow() != null) {
                    dialogError.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
                }

                TextView txtTitulo = dialogError.findViewById(R.id.txt_titulo_error);
                TextView txtMensaje = dialogError.findViewById(R.id.txt_mensaje_error);

                if (partida.isBloqueada()) {
                    if (txtTitulo != null) txtTitulo.setText("Bloqueado");
                    if (txtMensaje != null)
                        txtMensaje.setText("No tienes la temática: " + partida.getTematica());
                } else {
                    if (txtTitulo != null) txtTitulo.setText("Sala Llena");
                    if (txtMensaje != null)
                        txtMensaje.setText("La partida ya tiene " + partida.getJugadoresTexto() + " jugadores.");
                }

                dialogError.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(viewCerrar -> {
                    dialogError.dismiss();
                });

                dialogError.show();

            } else {
                Intent intent = new Intent(MisionPublicaActivity.this, SalaEsperaActivity.class);
                intent.putExtra("TEMATICA_PARTIDA", partida.getTematica());
                startActivity(intent);
            }
        });

        if (recyclerMisiones != null) {
            recyclerMisiones.setAdapter(adapter);
        }
    }
}