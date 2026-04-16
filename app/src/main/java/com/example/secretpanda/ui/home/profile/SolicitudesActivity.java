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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SolicitudesActivity extends AppCompatActivity {

    private View tabEnviar, tabRecibidas, tabPendientes;
    private View layoutEnviar, layoutRecibidas, layoutPendientes;

    private RecyclerView recyclerRecibidas, recyclerPendientes;
    private SolicitudAdapter adapterRecibidas;
    private SolicitudPendienteAdapter adapterPendientes;

    private EditText etBuscarAmigo;
    private View btnEnviarSolicitud;
    private TextView txtFeedback;
    private TextView txtMensajeRecibidas;

    private List<Solicitud> listaRecibidas = new ArrayList<>();
    private List<String> listaPendientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_solicitudes);

        initViews();
        setupRecyclerViews();
        setupTabs();
        setupSearch();

        cargarSolicitudesRecibidasServidor();
    }

    private void initViews() {
        tabEnviar = findViewById(R.id.tab_enviar);
        tabRecibidas = findViewById(R.id.tab_recibidas);
        tabPendientes = findViewById(R.id.tab_pendientes);

        layoutEnviar = findViewById(R.id.layout_enviar_solicitud);
        layoutRecibidas = findViewById(R.id.layout_solicitudes_recibidas);
        layoutPendientes = findViewById(R.id.layout_solicitudes_pendientes);

        etBuscarAmigo = findViewById(R.id.input_nombre_solicitud);
        btnEnviarSolicitud = findViewById(R.id.btn_enviar_solicitud);
        txtFeedback = findViewById(R.id.txt_feedback_solicitud);
        txtMensajeRecibidas = findViewById(R.id.texto_mensaje_recibidas);

        findViewById(R.id.btn_cerrar_solicitudes).setOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        recyclerRecibidas = findViewById(R.id.recycler_solicitudes_recibidas);
        recyclerRecibidas.setLayoutManager(new LinearLayoutManager(this));
        adapterRecibidas = new SolicitudAdapter(listaRecibidas, new SolicitudAdapter.OnSolicitudActionListener() {
            @Override
            public void onAceptar(Solicitud solicitud) {
                responderSolicitud(solicitud, "aceptada");
            }

            @Override
            public void onRechazar(Solicitud solicitud) {
                responderSolicitud(solicitud, "rechazada");
            }
        });
        recyclerRecibidas.setAdapter(adapterRecibidas);

        recyclerPendientes = findViewById(R.id.recycler_solicitudes_pendientes);
        recyclerPendientes.setLayoutManager(new LinearLayoutManager(this));
        adapterPendientes = new SolicitudPendienteAdapter(listaPendientes, (pos, nombre) -> {});
        recyclerPendientes.setAdapter(adapterPendientes);
    }

    private void setupTabs() {
        tabEnviar.setOnClickListener(v -> switchTab(1));
        tabRecibidas.setOnClickListener(v -> switchTab(2));
        tabPendientes.setOnClickListener(v -> switchTab(3));
        switchTab(2);
    }

    private void switchTab(int index) {
        layoutEnviar.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        layoutRecibidas.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        layoutPendientes.setVisibility(index == 3 ? View.VISIBLE : View.GONE);

        tabEnviar.setBackgroundColor(index == 1 ? Color.parseColor("#E5F3F5") : Color.parseColor("#5C7A99"));
        tabRecibidas.setBackgroundColor(index == 2 ? Color.parseColor("#E5F3F5") : Color.parseColor("#5C7A99"));
        tabPendientes.setBackgroundColor(index == 3 ? Color.parseColor("#E5F3F5") : Color.parseColor("#5C7A99"));

        ((TextView) tabEnviar).setTextColor(index == 1 ? Color.parseColor("#333333") : Color.WHITE);
        ((TextView) tabRecibidas).setTextColor(index == 2 ? Color.parseColor("#333333") : Color.WHITE);
        ((TextView) tabPendientes).setTextColor(index == 3 ? Color.parseColor("#333333") : Color.WHITE);

        if (index == 2) cargarSolicitudesRecibidasServidor();
        if (index == 3) cargarSolicitudesPendientesServidor();
        if (index == 1 && txtFeedback != null) txtFeedback.setText("");
        if (txtMensajeRecibidas != null) txtMensajeRecibidas.setVisibility(View.INVISIBLE);
    }

    private void cargarSolicitudesPendientesServidor() {
        TokenManager tm = new TokenManager(this);
        String jwt = tm.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/amigos/solicitudes")
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONArray array = new JSONArray(json);
                        List<String> temp = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            temp.add(obj.getString("tag_solicitante"));
                        }
                        runOnUiThread(() -> {
                            listaPendientes.clear();
                            listaPendientes.addAll(temp);
                            if (adapterPendientes != null) adapterPendientes.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {}
                }
            }
        });
    }

    private void cargarSolicitudesRecibidasServidor() {
        TokenManager tm = new TokenManager(this);
        String jwt = tm.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/amigos/solicitudes")
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API", "Error red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONArray array = new JSONArray(json);
                        List<Solicitud> temp = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            temp.add(new Solicitud(
                                    obj.getString("id_solicitante"),
                                    obj.getString("tag_solicitante"),
                                    obj.getString("foto_perfil_solicitante"),
                                    obj.getString("fecha_solicitud"),
                                    obj.getString("estado")
                            ));
                        }
                        runOnUiThread(() -> {
                            listaRecibidas.clear();
                            listaRecibidas.addAll(temp);
                            adapterRecibidas.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        Log.e("API", "Error parseo: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void responderSolicitud(Solicitud s, String nuevoEstado) {
        TokenManager tm = new TokenManager(this);
        String jwt = tm.getToken();
        if (jwt == null) return;

        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("id_solicitante", s.getIdSolicitante());
            bodyJson.put("estado", nuevoEstado);
        } catch (JSONException e) { return; }

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/amigos/solicitudes")
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        listaRecibidas.remove(s);
                        adapterRecibidas.notifyDataSetChanged();
                        if (txtMensajeRecibidas != null) {
                            txtMensajeRecibidas.setText(nuevoEstado.equals("aceptada") ? "✔ Solicitud aceptada" : "✘ Solicitud rechazada");
                            txtMensajeRecibidas.setTextColor(nuevoEstado.equals("aceptada") ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
                            txtMensajeRecibidas.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void setupSearch() {
        btnEnviarSolicitud.setOnClickListener(v -> {
            String tag = etBuscarAmigo.getText().toString().trim();
            if (tag.isEmpty()) return;
            enviarSolicitud(tag);
        });
    }

    private void enviarSolicitud(String tag) {
        TokenManager tm = new TokenManager(this);
        String jwt = tm.getToken();
        if (jwt == null) return;

        // Mostrar estado inicial
        if (txtFeedback != null) {
            txtFeedback.setVisibility(View.VISIBLE);
            txtFeedback.setTextColor(Color.BLACK);
            txtFeedback.setText("Enviando...");
        }

        JSONObject bodyJson = new JSONObject();
        try { bodyJson.put("tag_receptor", tag); } catch (JSONException e) { return; }

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/amigos/solicitudes")
                .post(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (txtFeedback != null) {
                        txtFeedback.setTextColor(Color.parseColor("#F44336"));
                        txtFeedback.setText("Error de red");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int code = response.code();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        txtFeedback.setTextColor(Color.parseColor("#4CAF50")); // Verde
                        txtFeedback.setText("¡Solicitud enviada con éxito!");
                        etBuscarAmigo.setText("");
                    } else {
                        // ERRORES
                        txtFeedback.setTextColor(Color.parseColor("#F44336")); // Rojo
                        if (code == 409) {
                            txtFeedback.setText("Este usuario ya es tu amigo o tiene una solicitud pendiente.");
                        } else if (code == 404) {
                            txtFeedback.setText("El usuario no existe.");
                        } else {
                            txtFeedback.setText("No se pudo enviar la solicitud.");
                        }
                    }
                });
            }
        });
    }
}