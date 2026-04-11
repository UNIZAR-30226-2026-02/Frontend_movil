package com.example.secretpanda.ui.home.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

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

    private ArrayList<String> listaSolicitudesPendientes = new ArrayList<>();
    private ArrayList<Solicitud> listaSolicitudesRecibidas = new ArrayList<>();

    private StompClient stompClient;
    private TextView txtFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Cargar el diseño de la pantalla
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_solicitudes);

        //  Enlazar los botones y vistas del XML
        configurarNavegacionInferior();

        View btnCerrar = findViewById(R.id.btn_cerrar_solicitudes);
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> finish());

        caja1 = findViewById(R.id.tab_enviar);
        caja2 = findViewById(R.id.tab_recibidas);
        caja3 = findViewById(R.id.tab_pendientes);

        pantallaAnadir = findViewById(R.id.layout_enviar_solicitud);
        pantallaRecibidas = findViewById(R.id.layout_solicitudes_recibidas);
        pantallaPendientes = findViewById(R.id.layout_solicitudes_pendientes);

        txtFeedback = findViewById(R.id.txt_feedback_solicitud);

        etBuscarAmigo = findViewById(R.id.input_nombre_solicitud);
        btnEnviarSolicitud = findViewById(R.id.btn_enviar_solicitud);

        recyclerRecibidas = findViewById(R.id.recycler_solicitudes_recibidas);
        recyclerPendientes = findViewById(R.id.recycler_solicitudes_pendientes);

        if (recyclerRecibidas != null) recyclerRecibidas.setLayoutManager(new LinearLayoutManager(this));
        if (recyclerPendientes != null) recyclerPendientes.setLayoutManager(new LinearLayoutManager(this));

        if (caja1 != null) caja1.setOnClickListener(v -> mostrarPestana(1));
        if (caja2 != null) caja2.setOnClickListener(v -> mostrarPestana(2));
        if (caja3 != null) caja3.setOnClickListener(v -> mostrarPestana(3));

        // Inicializar Listas y Adaptadores
        listaSolicitudesPendientes = new ArrayList<>();
        listaSolicitudesRecibidas = new ArrayList<>();

        adapterRecibidas = new SolicitudAdapter(listaSolicitudesRecibidas, new SolicitudAdapter.OnAccionSolicitudListener() {
            @Override
            public void onAceptar(int position, String nombre, String idSolicitante) {
                responderSolicitudServidor(idSolicitante, "aceptada", position, nombre);
            }

            @Override
            public void onRechazar(int position, String nombre, String idSolicitante) {
                responderSolicitudServidor(idSolicitante, "rechazada", position, nombre);
            }
        });

        if (recyclerRecibidas != null) recyclerRecibidas.setAdapter(adapterRecibidas);

        adapterPendientes = new SolicitudPendienteAdapter(listaSolicitudesPendientes, (position, nombre) -> {
            Toast.makeText(SolicitudesActivity.this, "Cancelar no implementado", Toast.LENGTH_SHORT).show();
        });

        if (recyclerPendientes != null) recyclerPendientes.setAdapter(adapterPendientes);

        // Iniciar la pantalla correctamente
        mostrarPestana(1);
        cargarSolicitudesRecibidasServidor();
        suscribirseASolicitudesEnTiempoReal();
        configurarBuscador();
    }

    // ==============================================================
    // EL NUEVO MÉTODO PARA CAMBIAR COLORES Y OCULTAR EL BUSCADOR
    // ==============================================================
    private void mostrarPestana(int numeroPestana) {
        if (pantallaAnadir != null) pantallaAnadir.setVisibility(View.GONE);
        if (pantallaRecibidas != null) pantallaRecibidas.setVisibility(View.GONE);
        if (pantallaPendientes != null) pantallaPendientes.setVisibility(View.GONE);
        if (etBuscarAmigo != null) etBuscarAmigo.setVisibility(View.GONE);
        if (btnEnviarSolicitud != null) btnEnviarSolicitud.setVisibility(View.GONE);

        // Escondemos el mensaje de feedback al cambiar de pestaña
        if (txtFeedback != null) txtFeedback.setVisibility(View.GONE);

        View[] todasLasCajas = {caja1, caja2, caja3};
        for (View caja : todasLasCajas) {
            if (caja != null) {
                caja.setBackgroundResource(R.drawable.fondo_gris_redondeado);
                if (caja instanceof TextView) ((TextView) caja).setTextColor(Color.parseColor("#AAAAAA"));
            }
        }

        View cajaActiva = null;

        if (numeroPestana == 1) {
            if (pantallaAnadir != null) pantallaAnadir.setVisibility(View.VISIBLE);
            if (etBuscarAmigo != null) etBuscarAmigo.setVisibility(View.VISIBLE);
            if (btnEnviarSolicitud != null) btnEnviarSolicitud.setVisibility(View.VISIBLE);
            cajaActiva = caja1;
        }
        else if (numeroPestana == 2) {
            if (pantallaRecibidas != null) pantallaRecibidas.setVisibility(View.VISIBLE);
            cajaActiva = caja2;


            cargarSolicitudesRecibidasServidor();
        }
        else if (numeroPestana == 3) {
            if (pantallaPendientes != null) pantallaPendientes.setVisibility(View.VISIBLE);
            cajaActiva = caja3;
        }

        if (cajaActiva != null) {
            cajaActiva.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
            if (cajaActiva instanceof TextView) ((TextView) cajaActiva).setTextColor(Color.BLACK);
        }
    }


    // ==========================================
    // BUSCADOR DE AMIGOS
    // ==========================================
    private void configurarBuscador() {
        if (btnEnviarSolicitud != null && etBuscarAmigo != null) {
            btnEnviarSolicitud.setOnClickListener(v -> {
                String nombreEscrito = etBuscarAmigo.getText().toString().trim();

                // Si está vacío, avisamos y cortamos la ejecución antes de llamar al servidor
                if (nombreEscrito.isEmpty()) {
                    mostrarFeedback("Por favor, escribe un nombre de usuario.", false);
                    return;
                }

                // Si no está vacío, enviamos
                enviarSolicitudServidor(nombreEscrito);
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
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/solicitudes";

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("tag_receptor", tagReceptor);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).post(body).addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> mostrarFeedback("Error de conexión al servidor", false));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Mostramos mensaje de éxito
                        mostrarFeedback("¡Solicitud enviada a " + tagReceptor + "!", true);
                        if (etBuscarAmigo != null) etBuscarAmigo.setText("");

                        // Añadimos a la lista de Pendientes localmente
                        if (adapterPendientes != null) {
                            adapterPendientes.addItem(tagReceptor);
                        }

                    } else if (response.code() == 404) {
                        mostrarFeedback("El usuario introducido no existe", false);
                    } else if (response.code() == 409) {
                        mostrarFeedback("Ya hay una solicitud o ya sois amigos", false);
                    } else {
                        mostrarFeedback("Error al enviar código: " + response.code(), false);
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

                        Log.d("API_SOLICITUDES", "JSON: " + jsonData);

                        for (int i = 0; i < array.length(); i++) {
                            org.json.JSONObject obj = array.getJSONObject(i);
                            // Extraemos el tag del solicitante (RF-24)

                            String id_solicitante = obj.getString("id_solicitante");
                            String tag_solicitante = obj.getString("tag_solicitante");
                            String foto_perfil = obj.getString("foto_perfil_solicitante");
                            String fecha_solicitud = obj.getString("fecha_solicitud");
                            String estado = obj.getString("estado");
                            Solicitud s = new Solicitud(id_solicitante, tag_solicitante, foto_perfil, fecha_solicitud, estado);
                            Log.d("API_SOLICITUDES", "Item JSON: " + s.getIdSolicitante());
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

    private void responderSolicitudServidor(String idSolicitante, String nuevoEstado, int position, String nombreAmigo) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/solicitudes";

        // 1. Preparamos el cuerpo de la petición (JSON)
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("estado", nuevoEstado);
            jsonBody.put("id_solicitante", idSolicitante);
            Log.d("API_SOLICITUDES", "JSON: " + jsonBody.toString());
            // Nota: Asumo "rechazada" para rechazar. Cámbialo si tu backend usa otra palabra.
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
    @android.annotation.SuppressLint("CheckResult")
    private void suscribirseASolicitudesEnTiempoReal() {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        // 1. La URL base de tu WebSocket en Spring Boot (suele acabar en /ws o /websocket)
        String urlConexion = "ws://10.0.2.2:8080/ws/websocket";

        // NOTA: Si usas NaikSoftware STOMP:
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, urlConexion);
        // Para autenticar, puedes enviar el token en la cabecera de conexión:
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + jwt));
        stompClient.connect(headers);

        String destinoSuscripcion = "/user/queue/solicitudes";

            // 2. Nos suscribimos al canal (Ejemplo con sintaxis STOMP estándar)

        stompClient.topic(destinoSuscripcion).subscribe(topicMessage -> {
            String payload = topicMessage.getPayload();
            procesarNuevasSolicitudes(payload);
        }, throwable -> {
            Log.e("WEBSOCKET", "Error en la suscripción: ", throwable);
        });


        Log.d("WEBSOCKET", "Suscrito a: " + destinoSuscripcion);
    }

    private void procesarNuevasSolicitudes(String jsonPayload) {
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray(jsonPayload);
            java.util.ArrayList<Solicitud> nuevasSolicitudes = new java.util.ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject obj = jsonArray.getJSONObject(i);

                // Mapeamos los datos del JSON (RF-24)
                String idSolicitante = obj.getString("id_solicitante");
                String tagSolicitante = obj.getString("tag_solicitante");
                String fotoPerfil = obj.optString("foto_perfil_solicitante", "");
                String fecha = obj.optString("fecha_solicitud", "");
                String estado = obj.getString("estado");

                // Solo mostramos las pendientes
                if ("pendiente".equalsIgnoreCase(estado)) {
                    // Ajusta la creación del objeto según el constructor de tu clase Solicitud
                    Solicitud solicitud = new Solicitud(idSolicitante, tagSolicitante, fotoPerfil, fecha, estado);
                    nuevasSolicitudes.add(solicitud);
                }
            }

            // 3. Actualizamos la interfaz en el Hilo Principal (UI Thread)
            runOnUiThread(() -> {
                listaSolicitudesRecibidas.clear();
                listaSolicitudesRecibidas.addAll(nuevasSolicitudes);

                if (adapterRecibidas != null) {
                    adapterRecibidas.notifyDataSetChanged();
                }

                // Actualizar textos si la lista está vacía, etc.
                Log.d("WEBSOCKET", "Lista de solicitudes actualizada en tiempo real.");
            });

        } catch (org.json.JSONException e) {
            Log.e("WEBSOCKET", "Error parseando el array de solicitudes", e);
        }
    }

    private void mostrarFeedback(String mensaje, boolean esExito) {
        if (txtFeedback != null) {
            txtFeedback.setVisibility(View.VISIBLE);
            txtFeedback.setText(mensaje);
            if (esExito) {
                txtFeedback.setTextColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                txtFeedback.setTextColor(Color.parseColor("#F44336")); // Rojo
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desconectar el WebSocket cuando destruimos la Activity

        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }

    }

}