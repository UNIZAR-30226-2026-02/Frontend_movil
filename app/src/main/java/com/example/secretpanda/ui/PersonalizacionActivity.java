package com.example.secretpanda.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.data.model.ItemPersonalizacion;

import java.util.ArrayList;
import java.util.List;

public class PersonalizacionActivity extends AppCompatActivity {

    private LinearLayout tabBarajas, tabBordes, tabFondos;
    private TextView txtTabBarajas, txtTabBordes, txtTabFondos;

    private TextView txtSeccionActual, txtTematicaSeleccionada, txtVacioPosesion;
    private LinearLayout layoutTematicaSeleccionada;

    private RecyclerView recyclerPosesion, recyclerBloqueados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_personalizacion);

        configurarNavegacionInferior();

        // 1. ENLAZAR COMPONENTES
        tabBarajas = findViewById(R.id.tab_barajas);
        tabBordes = findViewById(R.id.tab_bordes);
        tabFondos = findViewById(R.id.tab_fondos);
        txtTabBarajas = findViewById(R.id.txt_tab_barajas);
        txtTabBordes = findViewById(R.id.txt_tab_bordes);
        txtTabFondos = findViewById(R.id.txt_tab_fondos);

        txtSeccionActual = findViewById(R.id.txt_seccion_actual);
        txtTematicaSeleccionada = findViewById(R.id.txt_tematica_seleccionada);
        txtVacioPosesion = findViewById(R.id.txt_vacio_posesion);
        layoutTematicaSeleccionada = findViewById(R.id.layout_tematica_seleccionada);

        recyclerPosesion = findViewById(R.id.recycler_posesion);
        recyclerBloqueados = findViewById(R.id.recycler_bloqueados);

        recyclerPosesion.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBloqueados.setLayoutManager(new GridLayoutManager(this, 3));

        // 2. ACCIONES DE LAS PESTAÑAS
        tabBarajas.setOnClickListener(v -> seleccionarPestana(
                tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, tabFondos, txtTabFondos, "Temática barajas"));

        tabBordes.setOnClickListener(v -> seleccionarPestana(
                tabBordes, txtTabBordes, tabBarajas, txtTabBarajas, tabFondos, txtTabFondos, "Temática borde"));

        tabFondos.setOnClickListener(v -> seleccionarPestana(
                tabFondos, txtTabFondos, tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, "Temática fondo"));

        // Empezamos en Barajas
        cargarDatos("barajas");
    }

    private void seleccionarPestana(LinearLayout activa, TextView txtActivo,
                                    LinearLayout inactiva1, TextView txtInactivo1,
                                    LinearLayout inactiva2, TextView txtInactivo2,
                                    String titulo) {

        txtSeccionActual.setText(titulo);
        txtActivo.setVisibility(View.VISIBLE);
        txtInactivo1.setVisibility(View.GONE);
        txtInactivo2.setVisibility(View.GONE);

        activa.setBackgroundResource(R.drawable.bg_tab_activo);
        inactiva1.setBackgroundResource(R.drawable.bg_tab_inactivo);
        inactiva2.setBackgroundResource(R.drawable.bg_tab_inactivo);

        animarAltura(activa, 80);
        animarAltura(inactiva1, 50);
        animarAltura(inactiva2, 50);

        if (titulo.contains("barajas")) cargarDatos("barajas");
        else if (titulo.contains("borde")) cargarDatos("bordes");
        else cargarDatos("fondos");
    }

    private void animarAltura(View vista, int altoFinalDp) {
        float density = getResources().getDisplayMetrics().density;
        int finalPx = (int) (altoFinalDp * density);

        if (vista.getHeight() == finalPx) return;

        ValueAnimator animator = ValueAnimator.ofInt(vista.getHeight(), finalPx);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }

    // ==========================================
    // CEREBRO: DATOS SEGÚN PESTAÑA
    // ==========================================
    private void cargarDatos(String categoria) {
        List<ItemPersonalizacion> posesion = new ArrayList<>();
        List<ItemPersonalizacion> bloqueados = new ArrayList<>();
        boolean permiteSeleccion = false;

        if (categoria.equals("barajas")) {
            // Barajas NO se seleccionan. El texto de arriba desaparece.
            permiteSeleccion = false;
            layoutTematicaSeleccionada.setVisibility(View.GONE);

            posesion.add(new ItemPersonalizacion("Clásica", false));
            posesion.add(new ItemPersonalizacion("Lápices", false));
            posesion.add(new ItemPersonalizacion("Neón", false));

            bloqueados.add(new ItemPersonalizacion("Flores", true));
            bloqueados.add(new ItemPersonalizacion("Fuego", true));
        }
        else if (categoria.equals("bordes")) {
            // Bordes SÍ se seleccionan.
            permiteSeleccion = true;
            layoutTematicaSeleccionada.setVisibility(View.VISIBLE);

            posesion.add(new ItemPersonalizacion("Madera", false));
            posesion.add(new ItemPersonalizacion("Metal", false));

            bloqueados.add(new ItemPersonalizacion("Oro", true));
            bloqueados.add(new ItemPersonalizacion("Diamante", true));
        }
        else if (categoria.equals("fondos")) {
            // Fondos SÍ se seleccionan, pero vamos a SIMULAR QUE NO TIENES NINGUNO COMPRADO
            permiteSeleccion = true;
            layoutTematicaSeleccionada.setVisibility(View.VISIBLE);

            // "posesion" lo dejamos completamente vacío

            bloqueados.add(new ItemPersonalizacion("Océano", true));
            bloqueados.add(new ItemPersonalizacion("Selva", true));
            bloqueados.add(new ItemPersonalizacion("Volcán", true));
        }

        // --- Lógica del Estado Vacío ("No tienes adquiridos...") ---
        if (posesion.isEmpty()) {
            txtVacioPosesion.setVisibility(View.VISIBLE);
            recyclerPosesion.setVisibility(View.GONE);
            txtTematicaSeleccionada.setText("Ninguna"); // Como no tienes, pones Ninguna
        } else {
            txtVacioPosesion.setVisibility(View.GONE);
            recyclerPosesion.setVisibility(View.VISIBLE);

            // Automáticamente actualizamos el texto al primer elemento que tengas
            if (permiteSeleccion) {
                txtTematicaSeleccionada.setText(posesion.get(0).getNombre());
            }
        }

        // Creamos los adaptadores pasándole la orden "permiteSeleccion"
        PersonalizacionAdapter adapterPosesion = new PersonalizacionAdapter(posesion, false, permiteSeleccion, item -> {
            // Esto se ejecuta cuando haces click en un cuadrado de Posesión (y solo si permiteSelección es true)
            txtTematicaSeleccionada.setText(item.getNombre());
        });

        PersonalizacionAdapter adapterBloqueados = new PersonalizacionAdapter(bloqueados, true, false, null);

        // Los inyectamos en la pantalla
        recyclerPosesion.setAdapter(adapterPosesion);
        recyclerBloqueados.setAdapter(adapterBloqueados);
    }

    private void configurarNavegacionInferior() {
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, TiendaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }
}