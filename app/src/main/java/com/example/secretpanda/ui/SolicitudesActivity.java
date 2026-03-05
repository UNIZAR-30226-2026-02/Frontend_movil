package com.example.secretpanda.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import java.util.Arrays;
import java.util.List;

public class SolicitudesActivity extends AppCompatActivity {

    private TextView tabEnviar, tabRecibidas, tabPendientes;
    private LinearLayout layoutEnviar, layoutRecibidas, layoutPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Enlazar vistas
        TextView btnCerrar = findViewById(R.id.btn_cerrar_solicitudes);
        tabEnviar = findViewById(R.id.tab_enviar);
        tabRecibidas = findViewById(R.id.tab_recibidas);
        tabPendientes = findViewById(R.id.tab_pendientes);

        layoutEnviar = findViewById(R.id.layout_enviar_solicitud);
        layoutRecibidas = findViewById(R.id.layout_solicitudes_recibidas);
        layoutPendientes = findViewById(R.id.layout_solicitudes_pendientes); // <-- ¡NUEVO!

        // (Aquí tenías lo del recyclerRecibidas...)

        // 1. Enlazar los textos de mensaje
        TextView textoMensajeEnviar = findViewById(R.id.texto_error_solicitud);
        TextView textoMensajeRecibidas = findViewById(R.id.texto_mensaje_recibidas);
        Button btnEnviar = findViewById(R.id.btn_enviar_solicitud);
        EditText inputNombre = findViewById(R.id.input_nombre_solicitud);

        // Nos aseguramos de que los mensajes estén ocultos al entrar
        textoMensajeEnviar.setVisibility(View.INVISIBLE);

        // =========================================================
        // LÓGICA DE LA PESTAÑA "ENVIAR"
        // =========================================================
        btnEnviar.setOnClickListener(v -> {
            String nombreIngresado = inputNombre.getText().toString().trim();

            textoMensajeEnviar.setVisibility(View.VISIBLE); // Mostramos el texto

            if (nombreIngresado.isEmpty()) {
                // ERROR: Campo vacío (Rojo)
                textoMensajeEnviar.setText("Por favor, introduce un nombre.");
                textoMensajeEnviar.setTextColor(android.graphics.Color.parseColor("#FF0000"));
            } else if (nombreIngresado.equals("UsuarioFalso")) {
                // ERROR: El usuario no existe (Rojo) (Cambia "UsuarioFalso" por tu lógica real luego)
                textoMensajeEnviar.setText("El usuario introducido no existe.");
                textoMensajeEnviar.setTextColor(android.graphics.Color.parseColor("#FF0000"));
            } else {
                // ÉXITO: Solicitud enviada (Verde oscuro)
                textoMensajeEnviar.setText("¡Solicitud enviada a " + nombreIngresado + "!");
                textoMensajeEnviar.setTextColor(android.graphics.Color.parseColor("#008000"));
                inputNombre.setText(""); // Vaciamos el campo tras enviarla
            }
        });

        // =========================================================
        // LÓGICA DE LA PESTAÑA "RECIBIDAS"
        // =========================================================
        RecyclerView recyclerRecibidas = findViewById(R.id.recycler_solicitudes_recibidas);
        List<String> solicitudesFalsas = Arrays.asList("RocioTryhard", "NinjaMaster", "ProGamer99");
        recyclerRecibidas.setLayoutManager(new LinearLayoutManager(this));

        // Le pasamos la lista de amigos y el "Listener" para escuchar los clicks
        recyclerRecibidas.setAdapter(new SolicitudAdapter(solicitudesFalsas, new SolicitudAdapter.OnAccionSolicitudListener() {
            @Override
            public void onAceptar(String nombre) {
                textoMensajeRecibidas.setText("Has aceptado a " + nombre);
                textoMensajeRecibidas.setTextColor(android.graphics.Color.parseColor("#008000")); // Verde
                textoMensajeRecibidas.setVisibility(View.VISIBLE);
                // Opcional: Aquí tendrías que borrar al usuario de la lista 'solicitudesFalsas' y actualizar el adapter
            }

            @Override
            public void onRechazar(String nombre) {
                textoMensajeRecibidas.setText("Has rechazado a " + nombre);
                textoMensajeRecibidas.setTextColor(android.graphics.Color.parseColor("#FF0000")); // Rojo
                textoMensajeRecibidas.setVisibility(View.VISIBLE);
                // Opcional: Aquí también lo borrarías de la lista
            }
        }));
        // ¡NUEVO! Configurar la lista de pendientes
        RecyclerView recyclerPendientes = findViewById(R.id.recycler_solicitudes_pendientes);
        recyclerPendientes.setLayoutManager(new LinearLayoutManager(this));

        // Datos de prueba
        List<String> pendientesFalsas = Arrays.asList("NinjaMaster", "ProGamer99");
        recyclerPendientes.setAdapter(new SolicitudPendienteAdapter(pendientesFalsas));
        // 3. Lógica de Pestañas
        tabEnviar.setOnClickListener(v -> cambiarPestana("ENVIAR"));
        tabRecibidas.setOnClickListener(v -> cambiarPestana("RECIBIDAS"));
        tabPendientes.setOnClickListener(v -> cambiarPestana("PENDIENTES"));

        // (Aquí va la lógica de enviar y cerrar que ya tenías)
        btnCerrar.setOnClickListener(v -> finish());
    }

    // Método que gestiona los colores y lo que se muestra
    private void cambiarPestana(String pestana) {
        // Colores base
        String colorSeleccionado = "#E5F3F5"; // Blanco azulado
        String colorDeseleccionado = "#5C7A99"; // Azul oscuro
        String textoOscuro = "#333333";
        String textoClaro = "#FFFFFF";

        // Resetear todas las pestañas a su estado deseleccionado
        tabEnviar.setBackgroundColor(Color.parseColor(colorDeseleccionado));
        tabEnviar.setTextColor(Color.parseColor(textoClaro));

        tabRecibidas.setBackgroundColor(Color.parseColor(colorDeseleccionado));
        tabRecibidas.setTextColor(Color.parseColor(textoClaro));

        tabPendientes.setBackgroundColor(Color.parseColor(colorDeseleccionado));
        tabPendientes.setTextColor(Color.parseColor(textoClaro));

        // Ocultar todos los contenedores
        layoutEnviar.setVisibility(View.GONE);
        layoutRecibidas.setVisibility(View.GONE);
        layoutPendientes.setVisibility(View.GONE);

        // Mostrar solo el que toca y pintarlo de seleccionado
        if (pestana.equals("ENVIAR")) {
            tabEnviar.setBackgroundColor(Color.parseColor(colorSeleccionado));
            tabEnviar.setTextColor(Color.parseColor(textoOscuro));
            layoutEnviar.setVisibility(View.VISIBLE);
        } else if (pestana.equals("RECIBIDAS")) {
            tabRecibidas.setBackgroundColor(Color.parseColor(colorSeleccionado));
            tabRecibidas.setTextColor(Color.parseColor(textoOscuro));
            layoutRecibidas.setVisibility(View.VISIBLE);
        } else if (pestana.equals("PENDIENTES")) {
            tabPendientes.setBackgroundColor(Color.parseColor(colorSeleccionado));
            tabPendientes.setTextColor(Color.parseColor(textoOscuro));
            layoutPendientes.setVisibility(View.VISIBLE); // (Para cuando hagas esta pantalla)
        }
    }
}
