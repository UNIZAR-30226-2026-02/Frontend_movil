package com.example.secretpanda.ui.home.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.SocialGlobal;
import com.example.secretpanda.ui.customization.PersonalizacionActivity;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.ui.shop.TiendaActivity;

import java.util.ArrayList;

public class SolicitudesActivity extends AppCompatActivity {

    // --- Pestañas Superiores ---
    private View caja1, caja2, caja3;
    private View pantallaAnadir, pantallaRecibidas, pantallaPendientes;

    // --- Listas ---
    private RecyclerView recyclerRecibidas, recyclerPendientes;
    private SolicitudAdapter adapterRecibidas;
    private SolicitudPendienteAdapter adapterPendientes;

    // --- Búsqueda ---
    private EditText etBuscarAmigo;
    private View btnEnviarSolicitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_solicitudes);

        configurarNavegacionInferior();

        // ==============================================================
        // 0. BOTÓN DE CERRAR LA PANTALLA (X)
        // ==============================================================
        // Buscamos el botón por los nombres más comunes que sueles poner
        View btnCerrar = findViewById(R.id.btn_cerrar_solicitudes);


        if (btnCerrar != null) {
            // "finish()" cierra esta Activity y te devuelve a la pantalla anterior
            btnCerrar.setOnClickListener(v -> finish());
        }

        // ==============================================================
        // 1. ENLAZAR PESTAÑAS (Revisa que los IDs coincidan con los tuyos)
        // ==============================================================
        caja1 = findViewById(R.id.tab_enviar); // Pestaña: Añadir Amigo
        caja2 = findViewById(R.id.tab_recibidas); // Pestaña: Recibidas
        caja3 = findViewById(R.id.tab_pendientes); // Pestaña: Pendientes

        pantallaAnadir = findViewById(R.id.layout_enviar_solicitud);     // Layout de buscar
        pantallaRecibidas = findViewById(R.id.layout_solicitudes_recibidas);  // Layout de recibidas
        pantallaPendientes = findViewById(R.id.layout_solicitudes_pendientes); // Layout de pendientes

        // ==============================================================
        // 2. ENLAZAR BUSCADOR Y RECYCLERS
        // ==============================================================
        etBuscarAmigo = findViewById(R.id.input_nombre_solicitud);
        btnEnviarSolicitud = findViewById(R.id.btn_enviar_solicitud);

        recyclerRecibidas = findViewById(R.id.recycler_solicitudes_recibidas);
        recyclerPendientes = findViewById(R.id.recycler_solicitudes_pendientes);

        if (recyclerRecibidas != null) recyclerRecibidas.setLayoutManager(new LinearLayoutManager(this));
        if (recyclerPendientes != null) recyclerPendientes.setLayoutManager(new LinearLayoutManager(this));

        // ==============================================================
        // 3. LÓGICA DE PESTAÑAS AL HACER CLIC
        // ==============================================================
        if (caja1 != null) caja1.setOnClickListener(v -> mostrarPestana(1));
        if (caja2 != null) caja2.setOnClickListener(v -> mostrarPestana(2));
        if (caja3 != null) caja3.setOnClickListener(v -> mostrarPestana(3));

        // Empezamos en la pestaña 1 ("Añadir Amigo") por defecto
        mostrarPestana(1);

        cargarDatos();
        configurarBuscador();
    }

    // ==============================================================
    // EL NUEVO MÉTODO PARA CAMBIAR COLORES Y OCULTAR EL BUSCADOR
    // ==============================================================
    private void mostrarPestana(int numeroPestana) {
        // 1. Ocultar todas las pantallas por precaución
        if (pantallaAnadir != null) pantallaAnadir.setVisibility(View.GONE);
        if (pantallaRecibidas != null) pantallaRecibidas.setVisibility(View.GONE);
        if (pantallaPendientes != null) pantallaPendientes.setVisibility(View.GONE);

        // ¡LA SOLUCIÓN A TU BUG! Ocultamos el buscador de forma forzosa
        if (etBuscarAmigo != null) etBuscarAmigo.setVisibility(View.GONE);
        if (btnEnviarSolicitud != null) btnEnviarSolicitud.setVisibility(View.GONE);

        // 2. Apagar los colores de todos los botones (Gris)
        View[] todasLasCajas = {caja1, caja2, caja3};
        for (View caja : todasLasCajas) {
            if (caja != null) {
                caja.setBackgroundResource(R.drawable.fondo_gris_redondeado);
                // Si tienes un texto dentro del botón, lo ponemos en blanco/grisáceo
                if (caja instanceof TextView) ((TextView) caja).setTextColor(Color.parseColor("#AAAAAA"));
            }
        }

        // 3. Encender solo la pestaña elegida (Color verde o blanco y mostrar su pantalla)
        View cajaActiva = null;

        if (numeroPestana == 1) {
            if (pantallaAnadir != null) pantallaAnadir.setVisibility(View.VISIBLE);
            // Volvemos a mostrar el buscador solo aquí
            if (etBuscarAmigo != null) etBuscarAmigo.setVisibility(View.VISIBLE);
            if (btnEnviarSolicitud != null) btnEnviarSolicitud.setVisibility(View.VISIBLE);
            cajaActiva = caja1;
        }
        else if (numeroPestana == 2) {
            if (pantallaRecibidas != null) pantallaRecibidas.setVisibility(View.VISIBLE);
            cajaActiva = caja2;
        }
        else if (numeroPestana == 3) {
            if (pantallaPendientes != null) pantallaPendientes.setVisibility(View.VISIBLE);
            cajaActiva = caja3;
        }

        // ¡Le damos el color de pestaña seleccionada!
        if (cajaActiva != null) {
            // Puedes cambiar fondo_blanco_redondeado por borde_verde_seleccion_personalizacion si prefieres
            cajaActiva.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
            if (cajaActiva instanceof TextView) ((TextView) cajaActiva).setTextColor(Color.BLACK);
        }
    }

    // ==========================================
    // CARGA DE DATOS Y ACCIONES (Tick / X)
    // ==========================================
    private void cargarDatos() {
        if (recyclerRecibidas != null) {
            adapterRecibidas = new SolicitudAdapter(new ArrayList<>(SocialGlobal.getInstance().getSolicitudesRecibidas()), new SolicitudAdapter.OnAccionSolicitudListener() {
                @Override
                public void onAceptar(int position, String nombre) {
                    adapterRecibidas.removeItem(position);
                    SocialGlobal.getInstance().aceptarSolicitud(nombre);
                    Toast.makeText(SolicitudesActivity.this, "¡Has aceptado a " + nombre + "!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRechazar(int position, String nombre) {
                    adapterRecibidas.removeItem(position);
                    SocialGlobal.getInstance().rechazarSolicitud(nombre);
                    Toast.makeText(SolicitudesActivity.this, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                }
            });
            recyclerRecibidas.setAdapter(adapterRecibidas);
        }

        if (recyclerPendientes != null) {
            adapterPendientes = new SolicitudPendienteAdapter(new ArrayList<>(SocialGlobal.getInstance().getSolicitudesPendientes()), new SolicitudPendienteAdapter.OnCancelarListener() {
                @Override
                public void onCancelar(int position, String nombre) {
                    adapterPendientes.removeItem(position);
                    SocialGlobal.getInstance().getSolicitudesPendientes().remove(nombre);
                    Toast.makeText(SolicitudesActivity.this, "Solicitud cancelada", Toast.LENGTH_SHORT).show();
                }
            });
            recyclerPendientes.setAdapter(adapterPendientes);
        }
    }

    // ==========================================
    // BUSCADOR DE AMIGOS
    // ==========================================
    private void configurarBuscador() {
        if (btnEnviarSolicitud != null && etBuscarAmigo != null) {
            btnEnviarSolicitud.setOnClickListener(v -> {
                String nombreEscrito = etBuscarAmigo.getText().toString().trim();

                if (nombreEscrito.isEmpty()) {
                    return;
                }
                if (!SocialGlobal.getInstance().existeUsuario(nombreEscrito)) {
                    Toast.makeText(this, "El usuario '" + nombreEscrito + "' no existe.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (SocialGlobal.getInstance().esMiAmigo(nombreEscrito)) {
                    Toast.makeText(this, "¡" + nombreEscrito + " ya es tu amigo!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (SocialGlobal.getInstance().yaEnvieSolicitud(nombreEscrito)) {
                    Toast.makeText(this, "Ya tienes una solicitud pendiente con " + nombreEscrito, Toast.LENGTH_SHORT).show();
                    return;
                }

                SocialGlobal.getInstance().enviarSolicitud(nombreEscrito);
                if (adapterPendientes != null) {
                    adapterPendientes.addItem(nombreEscrito);
                }
                etBuscarAmigo.setText("");

                Toast.makeText(this, "Solicitud enviada a " + nombreEscrito, Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ==========================================
    // BARRA DE NAVEGACIÓN INFERIOR
    // ==========================================
    private void configurarNavegacionInferior() {
        View btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(v -> {
                Intent intent = new Intent(SolicitudesActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        View btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                Intent intent = new Intent(SolicitudesActivity.this, TiendaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        View btnNavPersonalizacion = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizacion != null) {
            btnNavPersonalizacion.setOnClickListener(v -> {
                Intent intent = new Intent(SolicitudesActivity.this, PersonalizacionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }
}