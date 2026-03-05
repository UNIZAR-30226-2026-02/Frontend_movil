package com.example.secretpanda.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.secretpanda.R;
import com.example.secretpanda.ui.home.HomeActivity;

public class TiendaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Solo llamamos a setContentView UNA vez al principio
        setContentView(R.layout.activity_tienda);

        // Ocultar la barra superior por defecto
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Botón "Inicio" de la barra inferior
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio); // Asegúrate de haber corregido el XML a nav_inicio
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TiendaActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        // 2. Referencia a la casilla del CAFÉ
        LinearLayout itemCafe = findViewById(R.id.item_borde_cafe);
        if (itemCafe != null) {
            itemCafe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoCompra("Comprar borde", "Cafe", R.drawable.ic_launcher_background, 10000);
                }
            });
        }

        // 3. Referencia a la casilla del TABLERO DE MÚSICA
        LinearLayout itemTableroMusica = findViewById(R.id.item_tablero_musica);
        if (itemTableroMusica != null) {
            itemTableroMusica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoCompra("Comprar temática tablero", "Música", R.drawable.img, 10000);
                }
            });
        }
        LinearLayout itemBarajaAnimales = findViewById(R.id.item_baraja_animales);
        if (itemBarajaAnimales != null) {
            itemBarajaAnimales.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // La palabra "baraja" activará la estructura de cartas apiladas en el popup
                    mostrarDialogoCompra("Comprar temática barajas", "Animales", R.drawable.img_1, 10000);
                }
            });
        }
    }

    // Método genérico para mostrar el diálogo
    private void mostrarDialogoCompra(String tituloCabecera, String nombreItem, int imagenResId, int precioItem) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_comprar_item);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // 1. Enlazamos los elementos visuales
        android.widget.TextView tvCabecera = dialog.findViewById(R.id.tv_dialogo_cabecera);
        android.widget.TextView tvTitulo = dialog.findViewById(R.id.tv_dialogo_titulo);
        android.widget.TextView tvPrecio = dialog.findViewById(R.id.tv_dialogo_precio);
        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tv_dialogo_mensaje);
        android.widget.LinearLayout btnComprar = dialog.findViewById(R.id.btn_dialogo_comprar);
        android.widget.ImageView btnCerrar = dialog.findViewById(R.id.btn_cerrar_dialogo);

        // 🌟 NUEVO: Enlazamos las imágenes normales y las de la baraja
        android.widget.ImageView ivImagenNormal = dialog.findViewById(R.id.iv_dialogo_imagen);
        android.widget.FrameLayout layoutBaraja = dialog.findViewById(R.id.layout_dialogo_baraja);
        android.widget.ImageView ivImagenBaraja = dialog.findViewById(R.id.iv_dialogo_imagen_baraja);

        // 2. Rellenamos textos
        tvCabecera.setText(tituloCabecera);
        tvTitulo.setText(nombreItem);
        tvPrecio.setText(String.valueOf(precioItem));
        tvMensaje.setVisibility(View.INVISIBLE);

        // 3. LÓGICA DE IMÁGENES: ¿Es una baraja?
        if (tituloCabecera.toLowerCase().contains("baraja")) {
            // Es una baraja: ocultamos la imagen normal y mostramos las cartas apiladas
            ivImagenNormal.setVisibility(View.GONE);
            layoutBaraja.setVisibility(View.VISIBLE);
            ivImagenBaraja.setImageResource(imagenResId);
        } else {
            // Es un tablero o borde: mostramos la imagen normal y ocultamos las cartas apiladas
            layoutBaraja.setVisibility(View.GONE);
            ivImagenNormal.setVisibility(View.VISIBLE);
            ivImagenNormal.setImageResource(imagenResId);
        }
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int misMonedas = 15000; // Simulación de saldo

                if (misMonedas >= precioItem) {
                    tvMensaje.setText("¡Compra realizada!");
                    tvMensaje.setTextColor(android.graphics.Color.parseColor("#36D34B"));
                    tvMensaje.setVisibility(View.VISIBLE);

                    btnComprar.setVisibility(View.GONE);
                } else {
                    tvMensaje.setText("No tienes saldo suficiente");
                    tvMensaje.setTextColor(android.graphics.Color.BLACK);
                    tvMensaje.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
    }
}