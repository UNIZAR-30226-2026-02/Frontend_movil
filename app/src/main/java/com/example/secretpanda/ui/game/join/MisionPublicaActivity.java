package com.example.secretpanda.ui.game.join;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Partida;
import com.example.secretpanda.data.TokenManager; // Asegúrate de que este import apunte a tu TokenManager correcto
import com.example.secretpanda.ui.game.match.PartidaActivity;
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

public class MisionPublicaActivity extends AppCompatActivity {

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
            if (partida.isBloqueada() || partida.isLlena()) {
                mostrarDialogoError(partida);
            } else {
                int idPartidaClicada = partida.getIdPartida();

                // 1. PREPARAMOS LA LLAMADA AL SERVIDOR PARA UNIRNOS
                OkHttpClient client = new OkHttpClient();
                String url = "http://10.0.2.2:8080/api/partidas/" + idPartidaClicada + "/unirse";

                TokenManager tokenManager = new TokenManager(this);
                String token = tokenManager.getToken();

                // Mandamos un JSON vacío porque tu backend acepta dto nulo o vacío para partidas públicas
                okhttp3.RequestBody body = okhttp3.RequestBody.create("{}", okhttp3.MediaType.parse("application/json"));

                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .post(body);

                if (token != null && !token.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();

                // 2. HACEMOS LA PETICIÓN EN SEGUNDO PLANO
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("API_ERROR", "Error de red al intentar unirse", e);
                        runOnUiThread(() -> android.widget.Toast.makeText(MisionPublicaActivity.this, "Error de conexión", android.widget.Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // Si da 200 (éxito) O da 409 (ya estabas dentro), te dejamos pasar
                        if (response.isSuccessful() || response.code() == 409) {
                            runOnUiThread(() -> {
                                String miEquipo = "rojo";

                                Intent intent = new Intent(MisionPublicaActivity.this, PartidaActivity.class);
                                intent.putExtra("ID_PARTIDA", idPartidaClicada);
                                intent.putExtra("MI_EQUIPO", miEquipo);
                                intent.putExtra("ES_LIDER", false);
                                intent.putExtra("ES_PRIVADA", false);
                                intent.putExtra("MAX_JUGADORES", partida.getMaxJugadores());
                                intent.putExtra("TIEMPO_TURNO", partida.getTiempo());

                                startActivity(intent);
                            });
                        } else {
                            // Otros errores (404 no encontrada, 401 sin token, etc.)
                            Log.e("API_ERROR", "El servidor rechazó la entrada. Código: " + response.code());
                            runOnUiThread(() -> android.widget.Toast.makeText(MisionPublicaActivity.this, "No se pudo entrar a la partida", android.widget.Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        });

        // 3. Enganchamos el Adapter al RecyclerView
        if (recyclerMisiones != null) {
            recyclerMisiones.setLayoutManager(new LinearLayoutManager(this));
            recyclerMisiones.setAdapter(adapter);
        }

        obtenerTemasDelJugador();
        // 4. Por último, pedimos los datos reales al backend
        obtenerPartidasDelServidor();
    }

    // --- LÓGICA DE INTERFAZ Y DIÁLOGOS ---

    private void mostrarDialogoError(Partida partida) {
        Dialog dialogError = new Dialog(MisionPublicaActivity.this);
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
        String url = "http://10.0.2.2:8080/api/partidas/publicas";

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

}