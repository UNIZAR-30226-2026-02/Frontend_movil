package com.example.secretpanda.ui.game.join; // Asegúrate de que coincida con tu paquete real

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Partida;
import com.example.secretpanda.ui.game.match.PartidaAdapter;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MisionPrivadaActivity extends AppCompatActivity {

    private EditText etCodigoSala;
    private TextView btnUnirse;
    private FrameLayout btnHome;

    private RecyclerView recyclerMisiones;
    private FrameLayout btnCerrar;
    private LinearLayout btnSelectorTematicas;
    private TextView tvTematicaActual;

    private PartidaAdapter adapter;
    private List<Partida> listaPartidasTodas;
    private List<Partida> listaPartidasFiltradas;
    private ua.naiksoftware.stomp.StompClient stompClient;
    private String tematicaFiltroActual = "Todas las temáticas";
    private List<String> misTemasAdquiridos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        // Cuidado aquí: asegúrate de que tu XML se llama así. Antes pusiste "activity_misiones_publicas"
        setContentView(R.layout.activity_misiones_publicas);

        //etCodigoSala = findViewById(R.id.et_codigo_sala);
        //btnUnirse = findViewById(R.id.btn_confirmar_union);
        btnHome = findViewById(R.id.btn_volver_home);

        // 2. Configurar los eventos de clic

        recyclerMisiones = findViewById(R.id.rv_misiones);
        btnCerrar = findViewById(R.id.btn_volver_home);
        btnSelectorTematicas = findViewById(R.id.btn_selector_tematicas);
        tvTematicaActual = findViewById(R.id.tv_tematica_actual);

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> finish());
        if (btnSelectorTematicas != null) btnSelectorTematicas.setOnClickListener(v -> mostrarDialogoFiltro());

        // 1. ¡SUPER IMPORTANTE! Inicializamos las listas ANTES de pedir datos al servidor
        listaPartidasTodas = new ArrayList<>();
        listaPartidasFiltradas = new ArrayList<>();

        // 2. Inicializamos el Adapter configurando qué pasa al hacer clic en una partida
        adapter = new PartidaAdapter(listaPartidasFiltradas, partida -> {
            // 1. Comprobamos si no se puede entrar (llena o bloqueada por algún motivo)
            if (partida.isBloqueada() || partida.isLlena()) {
                mostrarDialogoError(partida);
            } else {
                // 2. ¡EL CAMBIO CLAVE!
                // En lugar de llamar al servidor aquí, simplemente abrimos la ventana
                // para pedirle la contraseña, pasándole el ID de la partida tocada.
                int idPartidaClicada = partida.getIdPartida(); // O partida.getId(), según tu modelo

                mostrarDialogoUnirsePrivada(idPartidaClicada);
            }
        });

        // 3. Enganchamos el Adapter al RecyclerView
        if (recyclerMisiones != null) {
            recyclerMisiones.setLayoutManager(new LinearLayoutManager(this));
            recyclerMisiones.setAdapter(adapter);
        }

        obtenerTemasDelJugador();
    }

    // --- LÓGICA DE INTERFAZ Y DIÁLOGOS ---

    private void mostrarDialogoError(Partida partida) {
        Dialog dialogError = new Dialog(MisionPrivadaActivity.this);
        dialogError.setContentView(R.layout.dialog_error_mision);

        if (dialogError.getWindow() != null) {
            dialogError.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtTitulo = dialogError.findViewById(R.id.txt_titulo_error);
        TextView txtMensaje = dialogError.findViewById(R.id.txt_mensaje_error);

        if (partida.isBloqueada()) {
            if (txtTitulo != null) txtTitulo.setText("Bloqueado");
            if (txtMensaje != null) txtMensaje.setText("No tienes la temática: " + partida.getTematica());
        } else {
            if (txtTitulo != null) txtTitulo.setText("Sala Llena");
            if (txtMensaje != null) txtMensaje.setText("La partida ya tiene " + partida.getJugadoresTexto() + " jugadores.");
        }

        dialogError.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(viewCerrar -> dialogError.dismiss());
        dialogError.show();
    }

    private void mostrarDialogoFiltro() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();
        dialog.setTematicaListener(tematica -> {
            tematicaFiltroActual = tematica;
            if (tvTematicaActual != null) {
                tvTematicaActual.setText(tematicaFiltroActual);
            }
            // Llamamos al filtro unificado
            filtrarMisionesPorTematica(tematicaFiltroActual);
        });
        dialog.show(getSupportFragmentManager(), "TematicasDialog");
    }

    // --- LÓGICA DE DATOS Y RED ---

    private void filtrarMisionesPorTematica(String tematica) {
        listaPartidasFiltradas.clear();

        if (tematica == null || tematica.equals("Todas las temáticas")) {
            listaPartidasFiltradas.addAll(listaPartidasTodas);
        } else {
            for (Partida partida : listaPartidasTodas) {
                if (partida.getTematica() != null && partida.getTematica().equals(tematica)) {
                    listaPartidasFiltradas.add(partida);
                }
            }
        }

        // Avisamos al adaptador de que la lista ha cambiado
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void obtenerPartidasDelServidor() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/partidas/privadas";

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Error de red al conectar con el servidor", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonRespuesta = response.body().string();
                    Log.d("API_JSON", "Datos del servidor: " + jsonRespuesta);

                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Partida>>(){}.getType();
                    List<Partida> partidasServidor = gson.fromJson(jsonRespuesta, listType);

                    runOnUiThread(() -> {
                        if (partidasServidor != null) {
                            listaPartidasTodas.clear();

                            // ---> LA MAGIA DEL BLOQUEO <---
                            for (Partida p : partidasServidor) {
                                // Si la temática de la partida NO está en mi lista de temas comprados...
                                if (!misTemasAdquiridos.contains(p.getTematica())) {
                                    // Bloqueamos la partida (Asegúrate de tener un setter en tu modelo Partida.java)
                                    ///Falta de hacer
                                }
                                listaPartidasTodas.add(p);
                            }

                            filtrarMisionesPorTematica(tematicaFiltroActual);
                        }
                    });
                } else {
                    Log.e("API_ERROR", "Código de error del servidor: " + response.code());
                }
            }
        });
    }

    private void obtenerTemasDelJugador() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/jugadores/temas";

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        Request.Builder requestBuilder = new Request.Builder().url(url).get();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_TEMAS", "Error al obtener temas", e);
                // Aunque falle, pedimos las partidas para no dejar la pantalla vacía
                obtenerPartidasDelServidor();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonRespuesta = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonRespuesta);
                        misTemasAdquiridos.clear();

                        // Guardamos los nombres de los temas que el jugador SÍ tiene
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject temaJson = jsonArray.getJSONObject(i);
                            misTemasAdquiridos.add(temaJson.getString("nombre"));
                        }
                    } catch (Exception e) {
                        Log.e("API_TEMAS", "Error procesando JSON de temas", e);
                    }
                }
                // ¡Magia en cadena! Una vez tenemos mis temas, pedimos las partidas públicas
                obtenerPartidasDelServidor();
            }
        });
    }



    private void conectarASalaPrivada(int idPartida, String codigo) {
        Toast.makeText(this, "Desencriptando acceso...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        // 1. Armamos el maletín JSON con los dos datos para tu backend
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("idPartida", idPartida);
            jsonBody.put("codigoPartida", codigo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        // 2. Disparamos al endpoint (Asegúrate de que esta URL es la de tu @PostMapping)
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://10.0.2.2:8080/api/partidas/unirse/privada")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MisionPrivadaActivity.this, "Error de radio: Servidor inalcanzable", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // ¡ÉXITO! Código correcto, entramos a la sala
                        Toast.makeText(MisionPrivadaActivity.this, "¡Acceso concedido!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MisionPrivadaActivity.this, SalaEsperaActivity.class);
                        intent.putExtra("ES_LIDER", false);
                        intent.putExtra("ES_PRIVADA", true);
                        intent.putExtra("ID_PARTIDA", idPartida); // Pasamos el ID real a la sala

                        startActivity(intent);
                        finish();
                    } else {
                        // ERROR (Código mal, partida llena, etc.)
                        String mensajeError = "Acceso denegado: Código incorrecto";
                        try {
                            org.json.JSONObject errorJson = new org.json.JSONObject(responseBody);
                            if (errorJson.has("message")) mensajeError = errorJson.getString("message");
                        } catch (Exception e) {}

                        Toast.makeText(MisionPrivadaActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    // ¡NUEVO! Ahora recibe el ID de la partida al abrirse
    public void mostrarDialogoUnirsePrivada(int idPartidaSeleccionada) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.activity_unirse_privada);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        android.widget.EditText etCodigoSala = dialog.findViewById(R.id.et_codigo_sala);
        android.widget.TextView btnUnirse = dialog.findViewById(R.id.btn_confirmar_union);
        android.widget.FrameLayout btnCancelar = dialog.findViewById(R.id.btn_volver_home);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnUnirse.setOnClickListener(v -> {
            String codigo = etCodigoSala.getText().toString().trim();

            if (codigo.isEmpty()) {
                etCodigoSala.setError("Debes introducir un código");
                android.widget.Toast.makeText(this, "Por favor, escribe el código de la sala", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                // ¡AQUÍ ESTÁ LA MAGIA! Pasamos el ID exacto y el código que ha escrito
                conectarASalaPrivada(idPartidaSeleccionada, codigo);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
