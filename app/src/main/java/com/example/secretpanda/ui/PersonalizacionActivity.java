package com.example.secretpanda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.home.HomeActivity;

public class PersonalizacionActivity extends AppCompatActivity {

    // Variables para las pantallas
    private View  pantallaTemaCartas, pantallaBordeCartas, pantallaTablero;

    // Variables para los botones de la barra
    private View caja1, caja2, caja3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Solo llamamos a setContentView UNA vez al principio
        setContentView(R.layout.activity_personalizacion);

        // Ocultar la barra superior por defecto
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Botón "Inicio" de la barra inferior
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio); // Asegúrate de haber corregido el XML a nav_inicio
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonalizacionActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda); // Asegúrate de haber corregido el XML a nav_inicio
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonalizacionActivity.this, TiendaActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        // Enlazamos las pantallas
        pantallaTemaCartas = findViewById(R.id.contenido_opcion_1);
        pantallaBordeCartas = findViewById(R.id.contenido_opcion_2);
        pantallaTablero = findViewById(R.id.contenido_opcion_3);

        // Enlazamos las cajas de la barra
        caja1 = findViewById(R.id.caja_1);
        caja2 = findViewById(R.id.caja_2);
        caja3 = findViewById(R.id.caja_3);
        caja2.setSelected(true);
        pantallaTemaCartas.setVisibility(View.GONE);
        pantallaBordeCartas.setVisibility(View.VISIBLE);
        // ACCIÓN: Botón 1 (Tienda)
        caja1.setOnClickListener(v -> {
            // Mostrar la tienda, ocultar el resto
            pantallaTemaCartas.setVisibility(View.VISIBLE);
            pantallaBordeCartas.setVisibility(View.GONE);
            pantallaTablero.setVisibility(View.GONE);
            // Animar los botones
            seleccionarCaja(caja1);
        });

        // ACCIÓN: Botón 2 (Personalización)
        caja2.setOnClickListener(v -> {
            // Mostrar personalización, ocultar tienda
            pantallaTemaCartas.setVisibility(View.GONE);
            pantallaBordeCartas.setVisibility(View.VISIBLE);
            pantallaTablero.setVisibility(View.GONE);

            // Animar los botones
            seleccionarCaja(caja2);
        });
        // ACCIÓN: Botón 2 (Personalización)
        caja3.setOnClickListener(v -> {
            // Mostrar personalización, ocultar tienda
            pantallaTemaCartas.setVisibility(View.GONE);
            pantallaBordeCartas.setVisibility(View.GONE);
            pantallaTablero.setVisibility(View.VISIBLE);

            // Animar los botones
            seleccionarCaja(caja3);
        });
    }
    // Método de ayuda para hacer grande la caja seleccionada
    private void seleccionarCaja(View cajaActiva) {
        View[] todasLasCajas = {caja1, caja2, caja3};

        for (View caja : todasLasCajas) {
            if (caja == cajaActiva) {
                caja.setSelected(true);
                caja.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
                caja.setElevation(4f);
            } else {
                caja.setSelected(false);
                caja.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                caja.setElevation(1f);
            }
        }
    }
}
