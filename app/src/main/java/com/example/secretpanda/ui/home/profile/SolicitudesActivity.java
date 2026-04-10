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
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Solicitud;
import com.example.secretpanda.ui.customization.PersonalizacionActivity;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.ui.shop.TiendaActivity;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

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

    private ArrayList<Solicitud> listaSolicitudesPendientes = new ArrayList<>();
    private ArrayList<Solicitud> listaSolicitudesRecibidas = new ArrayList<>();

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

        listaSolicitudesPendientes = new ArrayList<>();
        listaSolicitudesRecibidas = new ArrayList<>();

        //cargarDatos();
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
            cargarSolicitudesRecibidasServidor();

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
            adapterRecibidas = new SolicitudAdapter(listaSolicitudesRecibidas, new SolicitudAdapter.OnAccionSolicitudListener() {
                @Override
                public void onAceptar(int position, String nombre, int idSolicitante) {
                    // Llamamos a la API con estado "aceptada"
                    responderSolicitudServidor(idSolicitante, "aceptada", position, nombre);
                }

                @Override
                public void onRechazar(int position, String nombre, int idSolicitante) {
                    // Llamamos a la API con estado "rechazada" (o el que uses para cancelar)
                    responderSolicitudServidor(idSolicitante, "rechazada", position, nombre);
                }
            });
            recyclerRecibidas.setAdapter(adapterRecibidas);
        }

        // ... (el código de recyclerPendientes se queda igual por ahora)
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
                enviarSolicitudServidor(nombreEscrito);
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


    private void enviarSolicitudServidor(String tagReceptor) {
        // 1. Validamos que haya token
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) {
            android.widget.Toast.makeText(this, "Error de sesión: No hay token", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/solicitudes";

        // 2. Creamos el JSON con el tag del receptor
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("tag_receptor", tagReceptor);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        // 3. Petición POST con autorización
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        // 4. Ejecutamos la llamada
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(SolicitudesActivity.this, "¡Solicitud enviada a " + tagReceptor + "!", Toast.LENGTH_SHORT).show();
                        // Limpiamos el buscador para que el usuario sepa que ha funcionado
                        if (etBuscarAmigo != null) etBuscarAmigo.setText("");
                    } else if (response.code() == 404) {
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Jugador no encontrado", android.widget.Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 409) {
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Ya hay una solicitud pendiente o ya sois amigos", android.widget.Toast.LENGTH_LONG).show();
                    } else {
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Error al enviar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void cargarSolicitudesRecibidasServidor() {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/solicitudes";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> android.util.Log.e("API_SOLICITUDES", "Error al cargar recibidas"));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONArray array = new org.json.JSONArray(jsonData);
                        ArrayList<Solicitud> nuevosNombres = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            org.json.JSONObject obj = array.getJSONObject(i);
                            // Extraemos el tag del solicitante (RF-24)

                            int id_solicitante = obj.getInt("id_solicitante");
                            String tag_solicitante = obj.getString("tag_solicitante");
                            String foto_perfil = obj.getString("foto_perfil_solicitante");
                            String fecha_solicitud = obj.getString("fecha_solicitud");
                            String estado = obj.getString("estado");
                            Solicitud s = new Solicitud(id_solicitante, tag_solicitante, foto_perfil, fecha_solicitud, estado);

                            nuevosNombres.add(s);

                            // Nota: Aquí también tienes disponible 'foto_perfil_solicitante'
                            // y 'fecha_solicitud' si decides mejorar el diseño del item.
                        }

                        runOnUiThread(() -> {
                            // Limpiamos y actualizamos la lista global
                            listaSolicitudesRecibidas.clear();
                            listaSolicitudesRecibidas.addAll(nuevosNombres);

                            // Notificamos al adaptador del cambio
                            if (adapterRecibidas != null) {
                                adapterRecibidas.notifyDataSetChanged();
                            }
                        });

                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void responderSolicitudServidor(int idSolicitante, String nuevoEstado, int position, String nombreAmigo) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/solicitudes";

        // 1. Preparamos el cuerpo de la petición (JSON)
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("id_solicitante", idSolicitante);
            // Nota: Asumo "rechazada" para rechazar. Cámbialo si tu backend usa otra palabra.
            jsonBody.put("estado", nuevoEstado);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        // 2. Construimos la petición PUT
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        // 3. Ejecutamos la llamada
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Error de red al responder", android.widget.Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Si el servidor confirma, eliminamos el item de la lista visualmente
                        if (adapterRecibidas != null) {
                            adapterRecibidas.removeItem(position);
                        }

                        // Mostramos mensaje de éxito
                        String mensaje = nuevoEstado.equals("aceptada") ?
                                "¡Has aceptado a " + nombreAmigo + "!" :
                                "Solicitud rechazada";
                        android.widget.Toast.makeText(SolicitudesActivity.this, mensaje, android.widget.Toast.LENGTH_SHORT).show();

                    } else {
                        android.widget.Toast.makeText(SolicitudesActivity.this, "Error: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}